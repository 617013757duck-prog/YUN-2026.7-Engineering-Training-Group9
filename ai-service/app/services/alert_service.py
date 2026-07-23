"""
安全告警系统（基于滑动窗口阈值）
"""
from typing import Dict, List
from datetime import datetime, timedelta
from loguru import logger
from app.core.database import SessionLocal
from app.services.audit_service import AuditService


class AlertService:
    """安全告警系统"""

    # 告警规则
    ALERT_THRESHOLDS = {
        "prompt_injection_rate": 5,      # 5分钟内超过此数触发告警
        "security_block_rate": 10,       # 10分钟内超过此数触发告警
        "consecutive_errors": 3,          # 连续错误次数
    }

    def __init__(self):
        self.audit = AuditService()

    def check_and_alert(self, visit_id: int) -> List[Dict]:
        """检查是否需要触发安全告警"""
        alerts = []
        now = datetime.now()

        alerts.extend(self._check_injection_rate(now))
        alerts.extend(self._check_block_rate(now))
        alerts.extend(self._check_consecutive_errors(now))

        # 写入告警记录
        for alert in alerts:
            self.audit.log(
                event_type="SAFETY_BLOCK",
                user_id=None,
                visit_id=visit_id,
                details=alert,
            )
            logger.warning(f"安全告警: {alert['type']} (等级: {alert['level']})")

        return alerts

    def _check_injection_rate(self, now: datetime) -> List[Dict]:
        """检查提示词注入频率"""
        db = SessionLocal()
        try:
            from sqlalchemy import text
            threshold_time = now - timedelta(minutes=5)
            result = db.execute(text("""
                SELECT COUNT(*) as cnt FROM audit_logs
                WHERE event_type = 'PROMPT_INJECTION'
                  AND created_at > :threshold
            """), {"threshold": threshold_time}).fetchone()

            if result and result[0] >= self.ALERT_THRESHOLDS["prompt_injection_rate"]:
                return [{"level": "high", "type": "提示词注入频率异常",
                         "count": result[0], "window": "5分钟"}]
            return []
        finally:
            db.close()

    def _check_block_rate(self, now: datetime) -> List[Dict]:
        """检查安全拦截频率"""
        db = SessionLocal()
        try:
            from sqlalchemy import text
            threshold_time = now - timedelta(minutes=10)
            result = db.execute(text("""
                SELECT COUNT(*) as cnt FROM audit_logs
                WHERE event_type = 'SAFETY_BLOCK'
                  AND created_at > :threshold
            """), {"threshold": threshold_time}).fetchone()

            if result and result[0] >= self.ALERT_THRESHOLDS["security_block_rate"]:
                return [{"level": "medium", "type": "安全拦截频率异常",
                         "count": result[0], "window": "10分钟"}]
            return []
        finally:
            db.close()

    def _check_consecutive_errors(self, now: datetime) -> List[Dict]:
        """检查连续 Agent 错误"""
        db = SessionLocal()
        try:
            from sqlalchemy import text
            result = db.execute(text("""
                SELECT event_type FROM audit_logs
                WHERE event_type = 'AGENT_ERROR'
                ORDER BY created_at DESC LIMIT :limit
            """), {"limit": self.ALERT_THRESHOLDS["consecutive_errors"]}).fetchall()

            errors = [r[0] for r in result]
            if len(errors) >= self.ALERT_THRESHOLDS["consecutive_errors"]:
                return [{"level": "high", "type": "连续Agent错误",
                         "count": len(errors)}]
            return []
        finally:
            db.close()
