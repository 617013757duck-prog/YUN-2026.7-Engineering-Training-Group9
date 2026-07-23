"""
多 Agent 危险复核 API 接口
POST /api/ai/analyze —— 核心业务入口（集成安全护栏）
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
from loguru import logger

router = APIRouter()

# 延迟初始化（在 main.py startup 中完成）
orchestrator = None


def init_orchestrator(llm_service, prompt_manager, retriever):
    """由 main.py startup 调用，完成 Agent 系统初始化"""
    global orchestrator
    from app.agents.orchestrator import AgentOrchestrator
    orchestrator = AgentOrchestrator(llm_service, prompt_manager, retriever)


class AgentAnalyzeRequest(BaseModel):
    user_symptoms: str = Field(..., description="用户症状描述", max_length=2000)
    visit_id: int = Field(..., description="就诊记录ID")
    user_id: Optional[int] = Field(None, description="患者用户ID（可选，用于审计）")


class AgentAnalyzeResponse(BaseModel):
    success: bool
    visit_id: int
    agent_count: int
    agent_runs: list
    final_result: dict
    safety_report: Optional[dict] = None
    error: Optional[str] = None


@router.post("/analyze", response_model=AgentAnalyzeResponse)
async def analyze_symptoms(req: AgentAnalyzeRequest):
    """
    多 Agent 危险复核接口（核心业务入口）

    流程：安全输入过滤 → 症状分析(Agent1) → 风险判断(Agent2+RAG)
         → 建议生成(Agent3) → 安全输出审查 → 隐私脱敏

    示例请求：
    {
        "user_symptoms": "头痛3天，偶尔伴有恶心，无发热",
        "visit_id": 1001
    }
    """
    if orchestrator is None:
        raise HTTPException(status_code=503, detail="Agent 系统未初始化")

    # 安全护栏：输入过滤
    from app.security.gateway import SafetyGateway
    gateway = SafetyGateway()

    input_result = gateway.input_guard.check(req.user_symptoms)
    if not input_result.passed:
        logger.warning(f"输入被拦截: {input_result.blocked_reasons}")
        return AgentAnalyzeResponse(
            success=False,
            visit_id=req.visit_id,
            agent_count=0,
            agent_runs=[],
            final_result={},
            safety_report={
                "blocked": True,
                "input_risk": input_result.risk_level,
                "blocked_reasons": input_result.blocked_reasons,
            },
            error="输入包含不安全内容，已被安全护栏拦截",
        )

    # 隐私脱敏
    privacy_result = gateway.privacy_guard.check_and_desensitize(
        input_result.sanitized_input
    )
    safe_input = privacy_result.desensitized_text
    if privacy_result.has_sensitive:
        logger.info(f"输入隐私脱敏: {len(privacy_result.sensitive_items)} 项")

    try:
        # 执行 Agent 管线
        result = orchestrator.run(
            user_symptoms=safe_input,
            visit_id=req.visit_id,
            user_id=req.user_id,
        )

        # 安全护栏：输出审查
        if result.get("success"):
            suggestions = result.get("final_result", {}).get("suggestions", "")
            output_result = gateway.output_guard.check(suggestions)

            safety_report = {
                "blocked": False,
                "input_risk": input_result.risk_level,
                "output_risk": output_result.risk_level,
                "has_privacy_issue": privacy_result.has_sensitive,
                "output_violations": output_result.violations,
            }

            # 如果输出有违规，使用净化后的版本
            if not output_result.passed:
                result["final_result"]["suggestions"] = output_result.sanitized_output
                logger.warning(f"输出审查未通过: {len(output_result.violations)} 项违规")
        else:
            safety_report = {"blocked": False, "input_risk": "safe"}

        result["safety_report"] = safety_report
        return AgentAnalyzeResponse(**result)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"分析失败: {str(e)}")
