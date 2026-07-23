"""
健康检查 API
"""
from fastapi import APIRouter
from datetime import datetime
from app.config import settings

router = APIRouter()


@router.get("/health")
async def health_check():
    """健康检查"""
    # 检查 FAISS 状态
    kb_stats = {"loaded": False, "total_vectors": 0}
    try:
        from app.knowledge_base.vector_store import VectorStore
        store = VectorStore.get_instance()
        kb_stats = store.get_stats()
        kb_stats["loaded"] = kb_stats["has_index"]
    except Exception:
        pass

    return {
        "status": "ok",
        "version": settings.APP_VERSION,
        "model": settings.DEEPSEEK_MODEL,
        "knowledge_base": kb_stats,
        "timestamp": datetime.now().isoformat(),
    }
