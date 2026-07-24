"""
AI 安全护栏 API
POST /api/ai/safety/check  — 安全审查
POST /api/ai/safety/desensitize  — 隐私脱敏
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from app.security.gateway import SafetyGateway
from loguru import logger

router = APIRouter()
gateway = SafetyGateway()


class SafetyCheckRequest(BaseModel):
    user_input: str = Field(..., description="用户原始输入", max_length=2000)
    ai_output: str = Field(..., description="AI 原始输出", max_length=5000)


class SafetyCheckResponse(BaseModel):
    success: bool
    report: dict


class DesensitizeRequest(BaseModel):
    text: str = Field(..., description="需要脱敏的文本")


class DesensitizeResponse(BaseModel):
    success: bool
    has_sensitive: bool
    desensitized_text: str
    sensitive_items: list


@router.post("/safety/check", response_model=SafetyCheckResponse)
async def check_safety(req: SafetyCheckRequest):
    """
    安全审查接口（输入过滤 + 隐私脱敏 + 输出审查）

    示例：
    {
        "user_input": "头痛3天，偶尔恶心",
        "ai_output": "根据你的症状，可能患有偏头痛..."
    }
    """
    try:
        report = gateway.process(req.user_input, req.ai_output)
        logger.info(
            f"安全审查: input_risk={report.input_risk}, "
            f"output_risk={report.output_risk}, blocked={report.blocked}"
        )
        return SafetyCheckResponse(success=True, report=report.__dict__)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"安全审查失败: {str(e)}")


@router.post("/safety/desensitize", response_model=DesensitizeResponse)
async def desensitize(req: DesensitizeRequest):
    """
    隐私脱敏接口

    示例：
    {"text": "张三，身份证号110101199001011234，手机号13812345678"}
    返回脱敏后的文本
    """
    try:
        from app.security.privacy_guard import PrivacyGuard
        pg = PrivacyGuard()
        result = pg.check_and_desensitize(req.text)
        return DesensitizeResponse(
            success=True,
            has_sensitive=result.has_sensitive,
            desensitized_text=result.desensitized_text,
            sensitive_items=result.sensitive_items,
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"隐私脱敏失败: {str(e)}")
