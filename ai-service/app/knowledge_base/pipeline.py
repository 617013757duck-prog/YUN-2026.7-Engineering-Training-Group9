"""
知识库构建完整流水线
"""
from loguru import logger
from app.knowledge_base.collector import GuidelineCollector
from app.knowledge_base.cleaner import GuidelineCleaner
from app.knowledge_base.splitter import GuidelineSplitter
from app.knowledge_base.embedder import BGEEmbedder
from app.knowledge_base.vector_store import VectorStore


class KnowledgeBasePipeline:
    """知识库构建完整流水线"""

    def __init__(self):
        self.collector = GuidelineCollector()
        self.cleaner = GuidelineCleaner()
        self.splitter = GuidelineSplitter(chunk_size=512, chunk_overlap=64)
        self.embedder = BGEEmbedder()
        self.store = VectorStore.get_instance()

    def run(self, raw_data_path: str):
        """
        执行完整流水线
        1. 收集原始数据
        2. 清洗数据
        3. 文档切分
        4. 向量化
        5. 存储到 FAISS
        """
        logger.info("[1/5] 收集原始数据...")
        raw_data = self.collector.collect_from_json(raw_data_path)

        logger.info("[2/5] 清洗数据...")
        for doc in raw_data:
            doc["content"] = self.cleaner.clean_text(doc.get("content", ""))
            doc["sections"] = self.cleaner.extract_sections(doc["content"])

        logger.info("[3/5] 文档切分...")
        chunks = self.splitter.split_documents(raw_data)

        logger.info(f"[4/5] 向量化 ({len(chunks)} 个文档片段)...")
        texts = [chunk["content"] for chunk in chunks]
        embeddings = self.embedder.encode_documents(texts)

        logger.info("[5/5] 存储到 FAISS...")
        self.store.build_index(embeddings, chunks)

        logger.info(f"知识库构建完成，共 {len(chunks)} 条向量，维度={self.embedder.dimension}")

    def run_from_texts(self, texts: list, sources: list = None):
        """从纯文本列表构建知识库"""
        if sources is None:
            sources = ["manual_input"] * len(texts)

        documents = []
        for i, (text, source) in enumerate(zip(texts, sources)):
            cleaned = self.cleaner.clean_text(text)
            documents.append({
                "doc_id": f"manual_{i:04d}",
                "title": source,
                "content": cleaned,
                "sections": self.cleaner.extract_sections(cleaned),
                "year": 2024,
            })

        return self.run_documents(documents)

    def run_documents(self, documents: list):
        """从已准备好的文档列表构建知识库"""
        logger.info("[1/3] 文档切分...")
        chunks = self.splitter.split_documents(documents)

        logger.info(f"[2/3] 向量化 ({len(chunks)} 个文档片段)...")
        texts = [chunk["content"] for chunk in chunks]
        embeddings = self.embedder.encode_documents(texts)

        logger.info("[3/3] 存储到 FAISS...")
        self.store.build_index(embeddings, chunks)

        logger.info(f"知识库构建完成，共 {len(chunks)} 条向量")
