"""
多 Agent 编排器
串联流程：症状分析 → 风险判断 → 建议生成
每一步的结果传递给下一步，完整调用链存储到 agent_runs 表
"""
import time
from typing import Dict, Any, List, Optional
from loguru import logger
from app.agents.base import AgentRun
from app.agents.symptom_agent import SymptomAnalysisAgent
from app.agents.risk_agent import RiskAssessmentAgent
from app.agents.suggestion_agent import SuggestionAgent


class AgentOrchestrator:
    """
    多 Agent 编排器
    串联流程：症状分析 → 风险判断 → 建议生成
    每一步的结果传递给下一步
    """

    def __init__(self, llm_service, prompt_manager, retriever):
        self.llm_service = llm_service
        self.agent_1 = SymptomAnalysisAgent(llm_service, prompt_manager)
        self.agent_2 = RiskAssessmentAgent(llm_service, prompt_manager, retriever)
        self.agent_3 = SuggestionAgent(llm_service, prompt_manager)

    def run(self, user_symptoms: str, visit_id: int,
            user_id: int = None) -> Dict[str, Any]:
        """
        执行完整的多 Agent 复核流程
        返回：所有 Agent 的执行记录 + 最终结果
        """
        start_time = time.time()
        agent_runs: List[AgentRun] = []
        logger.info(f"多 Agent 复核开始 (visit_id={visit_id})")

        # --- 审计日志：分析开始 ---
        self._audit_log("AI_ANALYZE_START", user_id, visit_id,
                        {"symptoms_length": len(user_symptoms)})

        try:
            # Step 1: 症状分析
            run_1 = self.agent_1.execute(user_symptoms=user_symptoms)
            agent_runs.append(run_1)

            if run_1.error:
                logger.error(f"症状分析失败: {run_1.error}")
                self._audit_log("AGENT_ERROR", user_id, visit_id,
                                {"agent": "symptom", "error": run_1.error})
                return self._error_response(agent_runs, "症状分析失败")

            symptom_result = run_1.output

            # Step 2: 风险判断（传入前一步结果 + RAG 检索）
            run_2 = self.agent_2.execute(
                symptom_analysis=symptom_result,
                user_symptoms=user_symptoms,
                visit_id=visit_id,
            )
            agent_runs.append(run_2)

            if run_2.error:
                logger.error(f"风险判断失败: {run_2.error}")
                self._audit_log("AGENT_ERROR", user_id, visit_id,
                                {"agent": "risk", "error": run_2.error})
                return self._error_response(agent_runs, "风险判断失败")

            risk_result = run_2.output

            # Step 3: 建议生成（传入前两步结果）
            run_3 = self.agent_3.execute(
                symptom_analysis=symptom_result,
                risk_assessment=risk_result,
            )
            agent_runs.append(run_3)

            end_time = time.time()

            # 汇总结果
            final = {
                "success": True,
                "visit_id": visit_id,
                "agent_count": 3,
                "agent_runs": [r.to_dict() for r in agent_runs],
                "final_result": {
                    "symptoms": symptom_result.get("structured_symptoms", []),
                    "red_flags": symptom_result.get("red_flags", []),
                    "has_red_flag": symptom_result.get("has_red_flag", False),
                    "risk_level": risk_result.get("risk_level", "未知"),
                    "is_emergency": risk_result.get("is_emergency", False),
                    "suggestions": run_3.output.get("suggestions", ""),
                    "citations": risk_result.get("citations", []),
                    "violations_detected": run_3.output.get("violations_detected", []),
                },
            }

            # --- 存储完整调用链 ---
            try:
                self._save_trace(
                    visit_id=visit_id, run_id=run_1.run_id,
                    user_symptoms=user_symptoms,
                    agent_runs=[run_1, run_2, run_3],
                    final_output=run_3.output.get("suggestions", ""),
                    start_time=start_time, end_time=end_time,
                )
            except Exception as e:
                logger.warning(f"调用链存储失败（数据库可能未连接）: {e}")

            # --- 审计日志：分析完成 ---
            self._audit_log("AI_ANALYZE_END", user_id, visit_id, {
                "risk_level": risk_result.get("risk_level"),
                "has_red_flag": symptom_result.get("has_red_flag", False),
                "duration_ms": round((end_time - start_time) * 1000, 2),
            })

            logger.info(
                f"多 Agent 复核完成: risk={final['final_result']['risk_level']}, "
                f"red_flag={final['final_result']['has_red_flag']}, "
                f"emergency={final['final_result']['is_emergency']}"
            )
            return final

        except Exception as e:
            self._audit_log("AGENT_ERROR", user_id, visit_id,
                            {"error": str(e)})
            return self._error_response(agent_runs, f"Agent 执行异常: {str(e)}")

    def _error_response(self, agent_runs: List[AgentRun], error_msg: str) -> Dict:
        return {
            "success": False,
            "error": error_msg,
            "agent_runs": [r.to_dict() for r in agent_runs],
            "final_result": {},
        }

    def _audit_log(self, event_type: str, user_id: Optional[int],
                   visit_id: Optional[int], details: dict):
        """写入审计日志（非阻塞，失败不影响主流程）"""
        try:
            from app.services.audit_service import AuditService
            AuditService().log(event_type, user_id, visit_id, details)
        except Exception:
            pass  # 审计日志失败不影响核心流程

    def _save_trace(self, visit_id: int, run_id: str,
                    user_symptoms: str, agent_runs: List[AgentRun],
                    final_output: str, start_time: float, end_time: float):
        """存储完整调用链路"""
        from app.services.trace_service import TraceService
        trace_service = TraceService()

        # 获取 RAG 检索结果（保存在 Agent 2 的输出中）
        risk_output = agent_runs[1].output if len(agent_runs) > 1 else {}

        trace_service.save_call_trace(
            visit_id=visit_id,
            run_id=run_id,
            user_input_raw=user_symptoms,
            user_input_desensitized=user_symptoms,  # 脱敏已在 API 层完成
            retriever_results=risk_output.get("citations", []),
            agent_1_output=agent_runs[0].output if len(agent_runs) > 0 else {},
            agent_2_output=agent_runs[1].output if len(agent_runs) > 1 else {},
            agent_3_output=agent_runs[2].output if len(agent_runs) > 2 else {},
            ai_output_raw=final_output,
            ai_output_sanitized=final_output,  # 安全审查在 API 层完成
            safety_check_passed=True,
            safety_violations=[],
            start_time=start_time,
            end_time=end_time,
        )
