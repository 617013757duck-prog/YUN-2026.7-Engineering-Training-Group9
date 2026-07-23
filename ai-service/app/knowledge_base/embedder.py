"""
BGE 文本向量化器
"""
import os
import numpy as np
from typing import List

# 必须在 SentenceTransformer 导入前设置 HuggingFace 镜像
from dotenv import load_dotenv
load_dotenv()
_hf = os.getenv("HF_ENDPOINT")
if _hf:
    os.environ["HF_ENDPOINT"] = _hf
# 强制离线模式（模型已缓存，跳过网络检查 BE adapter_config.json）
os.environ["HF_HUB_OFFLINE"] = "1"

from sentence_transformers import SentenceTransformer
from app.config import settings


class BGEEmbedder:
    """BGE 文本向量化器"""

    def __init__(self):
        self.model = SentenceTransformer(
            settings.EMBEDDING_MODEL_PATH,
            device=settings.EMBEDDING_DEVICE,
            local_files_only=True,  # 强制离线，模型已下载到本地缓存
        )
        self.dimension = self.model.get_sentence_embedding_dimension()

    def encode(self, texts: List[str], batch_size: int = 32) -> np.ndarray:
        """
        批量向量化
        对查询文本自动添加 instruction 前缀（BGE 模型要求）
        """
        return self.model.encode(
            texts,
            batch_size=batch_size,
            normalize_embeddings=True,   # L2 归一化，用于余弦相似度
            show_progress_bar=True,
        )

    def encode_query(self, query: str) -> np.ndarray:
        """查询向量化（自动添加 BGE instruction 前缀）"""
        instruction = "为这个句子生成表示以用于检索相关文章："
        return self.model.encode(
            [instruction + query],
            normalize_embeddings=True,
        )[0]

    def encode_documents(self, documents: List[str]) -> np.ndarray:
        """文档向量化（不添加 instruction）"""
        return self.encode(documents)
