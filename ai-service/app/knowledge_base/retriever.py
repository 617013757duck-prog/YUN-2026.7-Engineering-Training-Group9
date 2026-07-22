"""
医疗 RAG 检索器
"""
from typing import List, Dict, Optional
from datetime import datetime
from loguru import logger
from app.knowledge_base.embedder import BGEEmbedder
from app.knowledge_base.vector_store import VectorStore
from app.core.database import SessionLocal
from app.models.db_models import Citation
from app.config import settings


class MedicalRetriever:
    """医疗 RAG 检索器"""

    def __init__(self):
        self.embedder = BGEEmbedder()
        self.vector_store = VectorStore.get_instance()

    def retrieve(self, query: str, visit_id: int, top_k: int = None) -> List[Dict]:
        """
        完整检索链路：
        1. 用户症状向量化
        2. FAISS Top-K 相似度检索
        3. 引用记录写入 MySQL
        """
        if top_k is None:
            top_k = settings.TOP_K_RETRIEVAL

        # Step 1: 症状向量化
        query_embedding = self.embedder.encode_query(query)

        # Step 2: 相似度检索
        results = self.vector_store.search(query_embedding, top_k)

        # Step 3: 保存引用记录
        if results:
            self._save_citations(visit_id, results)

        return results

    def _save_citations(self, visit_id: int, results: List[Dict]):
        """保存引用记录到 MySQL"""
        db = SessionLocal()
        try:
            for r in results:
                citation = Citation(
                    visit_id=visit_id,
                    chunk_id=r.get("chunk_id", ""),
                    source=r.get("source", ""),
                    section=r.get("section", ""),
                    content=r.get("content", "")[:500],
                    score=r.get("score", 0.0),
                    created_at=datetime.now(),
                )
                db.add(citation)
            db.commit()
            logger.debug(f"已保存 {len(results)} 条引用记录 (visit_id={visit_id})")
        except Exception as e:
            db.rollback()
            logger.error(f"引用记录保存失败: {e}")
        finally:
            db.close()

    def format_context(self, results: List[Dict]) -> str:
        """将检索结果格式化为 LLM 可用的 context"""
        if not results:
            return "暂无相关指南参考。"

        context_parts = []
        for i, r in enumerate(results, 1):
            context_parts.append(
                f"[参考{i}] 来源: {r.get('source', '未知')}, "
                f"章节: {r.get('section', '未知')}\n"
                f"{r.get('content', '')}"
            )
        return "\n\n".join(context_parts)
