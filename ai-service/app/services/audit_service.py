"""
审计日志服务（与后端 audit_logs 表对齐）
"""
import json
from datetime import datetime
from typing import Optional
from loguru import logger
from app.core.database import SessionLocal


class AuditService:
    """审计日志服务"""

    EVENT_TYPES = {
        "AI_ANALYZE_START": "AI 分析开始",
        "AI_ANALYZE_END": "AI 分析完成",
        "SAFETY_BLOCK": "安全检查拦截",
        "SAFETY_PASS": "安全检查通过",
        "PRIVACY_DESENSITIZE": "隐私脱敏",
        "MODEL_VERSION_CHANGE": "模型版本变更",
        "KB_SNAPSHOT_CREATE": "知识库快照创建",
        "AGENT_ERROR": "Agent 执行错误",
        "PROMPT_INJECTION": "提示词注入检测",
        "UNAUTHORIZED_DIAGNOSIS": "越权诊断拦截",
    }

    def log(self, event_type: str, user_id: Optional[int],
            visit_id: Optional[int], details: dict,
            ip_address: str = "127.0.0.1"):
        """写入审计日志"""
        if event_type not in self.EVENT_TYPES:
            logger.warning(f"未知事件类型: {event_type}")
            return

        db = SessionLocal()
        try:
            # 使用原生 SQL 插入（与后端 audit_logs 表结构对齐）
            from sqlalchemy import text
            db.execute(text("""
                INSERT INTO audit_logs (event_type, user_id, visit_id, details, ip_address, created_at)
                VALUES (:event_type, :user_id, :visit_id, :details, :ip_address, :created_at)
            """), {
                "event_type": event_type,
                "user_id": user_id,
                "visit_id": visit_id,
                "details": json.dumps(details, ensure_ascii=False),
                "ip_address": ip_address,
                "created_at": datetime.now(),
            })
            db.commit()
            logger.debug(f"审计日志: {event_type}")
        except Exception as e:
            db.rollback()
            logger.error(f"审计日志写入失败: {e}")
        finally:
            db.close()
