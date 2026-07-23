"""
完整调用链路存储服务
"""
import time
from datetime import datetime
from typing import Dict, Any
from loguru import logger
from app.models.db_models import AgentRunRecord
from app.core.database import SessionLocal
from app.services.version_service import VersionService


class TraceService:
    """完整调用链路存储服务"""

    def __init__(self):
        self.version_service = VersionService()

    def save_call_trace(
        self, visit_id: int, run_id: str,
        user_input_raw: str, user_input_desensitized: str,
        retriever_results: list,
        agent_1_output: Dict, agent_2_output: Dict, agent_3_output: Dict,
        ai_output_raw: str, ai_output_sanitized: str,
        safety_check_passed: bool, safety_violations: list,
        start_time: float, end_time: float,
        input_tokens: int = 0, output_tokens: int = 0,
    ) -> int:
        """保存一条完整的调用链路记录"""
        db = SessionLocal()
        try:
            model_info = self.version_service.get_active_model()
            kb_info = self.version_service.get_latest_snapshot()

            record = AgentRunRecord(
                visit_id=visit_id,
                run_id=run_id,
                model_version_id=model_info["id"] if model_info else None,
                kb_snapshot_id=kb_info["id"] if kb_info else None,
                prompt_version=model_info["prompt_version"] if model_info else None,
                user_input_raw=user_input_raw,
                user_input_desensitized=user_input_desensitized,
                retriever_results=retriever_results,
                agent_1_output=agent_1_output,
                agent_2_output=agent_2_output,
                agent_3_output=agent_3_output,
                ai_output_raw=ai_output_raw,
                ai_output_sanitized=ai_output_sanitized,
                safety_check_passed=safety_check_passed,
                safety_violations=safety_violations,
                total_duration_ms=(end_time - start_time) * 1000,
                input_tokens=input_tokens,
                output_tokens=output_tokens,
                created_at=datetime.now(),
            )
            db.add(record)
            db.commit()
            logger.debug(f"调用链路已保存: run_id={run_id}")
            return record.id
        finally:
            db.close()

    def query_trace(self, visit_id: int) -> list:
        """查询某次就诊的完整调用链"""
        db = SessionLocal()
        try:
            records = db.query(AgentRunRecord).filter(
                AgentRunRecord.visit_id == visit_id
            ).order_by(AgentRunRecord.created_at.desc()).all()

            return [{
                "run_id": r.run_id, "visit_id": r.visit_id,
                "model_version_id": r.model_version_id,
                "kb_snapshot_id": r.kb_snapshot_id,
                "prompt_version": r.prompt_version,
                "ai_output_sanitized": r.ai_output_sanitized,
                "safety_check_passed": r.safety_check_passed,
                "total_duration_ms": r.total_duration_ms,
                "created_at": str(r.created_at),
            } for r in records]
        finally:
            db.close()
