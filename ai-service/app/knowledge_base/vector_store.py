"""
FAISS 向量数据库存储与索引
"""
import os
import json
import numpy as np
import faiss
from typing import List, Dict, Optional
from loguru import logger
from app.config import settings


class VectorStore:
    """FAISS 向量数据库（单例模式）"""

    _instance: Optional["VectorStore"] = None

    def __init__(self):
        self.index: Optional[faiss.Index] = None
        self.metadata: List[Dict] = []  # 存储每个向量对应的元数据
        self.dimension: Optional[int] = None

    @classmethod
    def get_instance(cls) -> "VectorStore":
        """获取单例实例"""
        if cls._instance is None:
            cls._instance = VectorStore()
        return cls._instance

    @classmethod
    def load_index(cls):
        """启动时加载已有索引"""
        instance = cls.get_instance()
        index_path = os.path.join(settings.FAISS_INDEX_PATH, "index.faiss")
        meta_path = os.path.join(settings.FAISS_INDEX_PATH, "metadata.json")

        if os.path.exists(index_path) and os.path.exists(meta_path):
            instance.index = faiss.read_index(index_path)
            with open(meta_path, "r", encoding="utf-8") as f:
                instance.metadata = json.load(f)
            instance.dimension = instance.index.d
            logger.info(f"已加载 FAISS 索引: {instance.index.ntotal} 条向量")
        else:
            logger.warning("FAISS 索引文件不存在，请先运行知识库构建流水线")

    def build_index(self, embeddings: np.ndarray, metadata: List[Dict]):
        """
        构建 FAISS 索引
        使用 IndexFlatIP（内积），配合 L2 归一化向量 = 余弦相似度
        """
        self.dimension = embeddings.shape[1]
        self.index = faiss.IndexFlatIP(self.dimension)  # Inner Product
        self.index.add(embeddings.astype(np.float32))
        self.metadata = metadata
        self._save_index()
        logger.info(f"FAISS 索引构建完成: {len(metadata)} 条向量, 维度={self.dimension}")

    def search(self, query_embedding: np.ndarray, top_k: int = None) -> List[Dict]:
        """
        相似度检索
        返回 Top-K 最相似文档
        """
        if top_k is None:
            top_k = settings.TOP_K_RETRIEVAL

        if self.index is None or self.index.ntotal == 0:
            logger.warning("FAISS 索引为空，无法检索")
            return []

        top_k = min(top_k, self.index.ntotal)
        query_vec = query_embedding.reshape(1, -1).astype(np.float32)
        scores, indices = self.index.search(query_vec, top_k)

        results = []
        for score, idx in zip(scores[0], indices[0]):
            if 0 <= idx < len(self.metadata):
                meta = self.metadata[idx].copy()
                meta["score"] = float(score)
                results.append(meta)

        return results

    def _save_index(self):
        """持久化索引到磁盘"""
        os.makedirs(settings.FAISS_INDEX_PATH, exist_ok=True)
        index_path = os.path.join(settings.FAISS_INDEX_PATH, "index.faiss")
        meta_path = os.path.join(settings.FAISS_INDEX_PATH, "metadata.json")
        faiss.write_index(self.index, index_path)
        with open(meta_path, "w", encoding="utf-8") as f:
            json.dump(self.metadata, f, ensure_ascii=False, indent=2)
        logger.info(f"FAISS 索引已保存到: {settings.FAISS_INDEX_PATH}")

    def get_stats(self) -> Dict:
        """获取索引统计信息"""
        return {
            "total_vectors": self.index.ntotal if self.index else 0,
            "dimension": self.dimension,
            "has_index": self.index is not None,
        }
