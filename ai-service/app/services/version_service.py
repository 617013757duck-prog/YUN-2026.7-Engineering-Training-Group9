"""
版本管理服务
"""
from datetime import datetime
from typing import Optional, Dict, List
from loguru import logger
from app.models.db_models import ModelVersion, KnowledgeBaseSnapshot
from app.core.database import SessionLocal


class VersionService:
    """版本管理服务"""

    # ---------- 模型版本 ----------

    def register_model(self, model_name: str, version: str,
                       api_endpoint: str, model_id: str,
                       prompt_version: str) -> ModelVersion:
        """注册新模型版本（LLM 统一使用 deepseek-v4-flash API）"""
        db = SessionLocal()
        try:
            # 将之前的 active 模型设为非活跃
            db.query(ModelVersion).filter(
                ModelVersion.is_active == True
            ).update({"is_active": False})

            mv = ModelVersion(
                model_name=model_name, version=version,
                api_endpoint=api_endpoint, model_id=model_id,
                prompt_version=prompt_version, is_active=True,
                deployed_at=datetime.now(), created_at=datetime.now(),
            )
            db.add(mv)
            db.commit()
            db.refresh(mv)
            logger.info(f"模型版本注册: {model_name} v{version}")
            return mv
        finally:
            db.close()

    def get_active_model(self) -> Optional[Dict]:
        """获取当前活跃的模型版本"""
        db = SessionLocal()
        try:
            mv = db.query(ModelVersion).filter(
                ModelVersion.is_active == True
            ).first()
            if mv:
                return {
                    "id": mv.id, "model_name": mv.model_name,
                    "version": mv.version, "model_id": mv.model_id,
                    "prompt_version": mv.prompt_version,
                }
            return None
        finally:
            db.close()

    def get_model_history(self) -> List[Dict]:
        """获取模型版本历史"""
        db = SessionLocal()
        try:
            versions = db.query(ModelVersion).order_by(
                ModelVersion.created_at.desc()
            ).all()
            return [
                {"id": v.id, "model_name": v.model_name,
                 "version": v.version, "deployed_at": str(v.deployed_at),
                 "is_active": v.is_active}
                for v in versions
            ]
        finally:
            db.close()

    # ---------- 知识库快照 ----------

    def create_snapshot(self, total_chunks: int, total_vectors: int,
                        embedding_model: str, chunk_size: int,
                        sources: List[str], index_hash: str) -> KnowledgeBaseSnapshot:
        """创建知识库版本快照"""
        db = SessionLocal()
        try:
            # 自增版本号
            latest = db.query(KnowledgeBaseSnapshot).order_by(
                KnowledgeBaseSnapshot.id.desc()
            ).first()

            if latest:
                parts = latest.version.split(".")
                new_version = f"{parts[0]}.{parts[1]}.{int(parts[2]) + 1}"
            else:
                new_version = "1.0.0"

            snapshot = KnowledgeBaseSnapshot(
                version=new_version, total_chunks=total_chunks,
                total_vectors=total_vectors, embedding_model=embedding_model,
                chunk_size=chunk_size, sources=sources,
                faiss_index_hash=index_hash,
                description=f"自动快照 - v{new_version}",
                created_at=datetime.now(),
            )
            db.add(snapshot)
            db.commit()
            db.refresh(snapshot)
            logger.info(f"知识库快照创建: v{new_version}")
            return snapshot
        finally:
            db.close()

    def get_latest_snapshot(self) -> Optional[Dict]:
        """获取最新知识库快照"""
        db = SessionLocal()
        try:
            snap = db.query(KnowledgeBaseSnapshot).order_by(
                KnowledgeBaseSnapshot.id.desc()
            ).first()
            if snap:
                return {
                    "id": snap.id,
                    "version": snap.version,
                    "total_chunks": snap.total_chunks,
                    "embedding_model": snap.embedding_model,
                }
            return None
        finally:
            db.close()
