"""
版本管理与审计 API
"""
from fastapi import APIRouter, Query
from app.services.version_service import VersionService
from app.services.trace_service import TraceService

router = APIRouter()
version_service = VersionService()
trace_service = TraceService()


@router.get("/version/model")
async def get_model_version():
    """获取当前活跃的模型版本（使用 deepseek-v4-flash API）"""
    mv = version_service.get_active_model()
    return {"success": True, "data": mv}


@router.get("/version/model/history")
async def get_model_history():
    """获取模型版本变更历史"""
    history = version_service.get_model_history()
    return {"success": True, "data": history}


@router.get("/version/knowledge-base")
async def get_kb_version():
    """获取最新知识库版本快照"""
    kb = version_service.get_latest_snapshot()
    return {"success": True, "data": kb}


@router.get("/trace/{visit_id}")
async def get_trace(visit_id: int):
    """查询某次就诊的完整 AI 调用链路"""
    traces = trace_service.query_trace(visit_id)
    return {"success": True, "visit_id": visit_id, "data": traces}


@router.get("/audit/recent")
async def get_recent_audit_logs(limit: int = Query(20, ge=1, le=100)):
    """查询最近审计日志"""
    from app.core.database import SessionLocal
    from sqlalchemy import text
    db = SessionLocal()
    try:
        results = db.execute(text(
            "SELECT id, event_type, user_id, visit_id, details, ip_address, created_at "
            "FROM audit_logs ORDER BY created_at DESC LIMIT :limit"
        ), {"limit": limit}).fetchall()
        return {"success": True, "data": [
            {"id": r[0], "event_type": r[1], "user_id": r[2],
             "visit_id": r[3], "details": r[4],
             "created_at": str(r[6])} for r in results
        ]}
    finally:
        db.close()


@router.get("/alerts")
async def get_alerts(visit_id: int = Query(0)):
    """获取当前安全告警"""
    from app.services.alert_service import AlertService
    alert_service = AlertService()
    alerts = alert_service.check_and_alert(visit_id=visit_id)
    return {"success": True, "data": alerts}
