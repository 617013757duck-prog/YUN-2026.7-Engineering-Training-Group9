"""
医疗指南文档切分器
"""
from typing import List, Dict
from langchain_text_splitters import RecursiveCharacterTextSplitter


class GuidelineSplitter:
    """医疗指南文档切分器"""

    def __init__(self, chunk_size: int = 512, chunk_overlap: int = 64):
        """
        chunk_size: 每个文档片段的字符数
        chunk_overlap: 相邻片段的重叠字符数
        """
        self.splitter = RecursiveCharacterTextSplitter(
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap,
            separators=["\n\n", "\n", "。", ".", "；", ";", "，", ",", " ", ""],
            length_function=len,
        )

    def split_documents(self, documents: List[Dict]) -> List[Dict]:
        """
        切分文档列表
        返回格式:
        [
            {
                "chunk_id": "guideline_001_chunk_0001",
                "source": "中国2型糖尿病防治指南",
                "section": "诊断标准",
                "content": "糖尿病诊断标准：空腹血糖≥7.0 mmol/L...",
                "page": 12
            },
            ...
        ]
        """
        chunks = []
        for doc in documents:
            sections = doc.get("sections", {})
            if not sections and doc.get("content"):
                # 如果没有分节，把整篇当作一个section
                sections = {"正文": doc["content"]}

            for section_title, section_content in sections.items():
                cleaned = section_content.strip()
                if len(cleaned) < 20:
                    continue
                texts = self.splitter.split_text(cleaned)
                for i, text in enumerate(texts):
                    chunks.append({
                        "chunk_id": f"{doc.get('doc_id', 'unknown')}_chunk_{i:04d}",
                        "source": doc.get("title", "未知"),
                        "section": section_title,
                        "content": text,
                        "page": doc.get("page", 0),
                        "year": doc.get("year", 2024),
                    })
        return chunks
