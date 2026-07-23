"""
医疗指南数据清洗器
"""
import re
from typing import List, Dict


class GuidelineCleaner:
    """医疗指南数据清洗器"""

    @staticmethod
    def clean_text(text: str) -> str:
        """清洗单条文本"""
        # 移除多余空白
        text = re.sub(r'\s+', ' ', text)
        # 移除特殊控制字符
        text = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]', '', text)
        # 统一标点（全角转半角）
        text = GuidelineCleaner._normalize_punctuation(text)
        return text.strip()

    @staticmethod
    def _normalize_punctuation(text: str) -> str:
        """标点标准化"""
        replacements = {
            '，': ',', '。': '.', '；': ';', '：': ':',
            '（': '(', '）': ')', '＂': '"', '＇': "'",
        }
        for full, half in replacements.items():
            text = text.replace(full, half)
        return text

    @staticmethod
    def filter_by_keywords(documents: List[Dict], keywords: List[str]) -> List[Dict]:
        """按关键词过滤相关文档"""
        filtered = []
        for doc in documents:
            content = doc.get("content", "")
            if any(kw in content for kw in keywords):
                filtered.append(doc)
        return filtered

    @staticmethod
    def extract_sections(text: str) -> Dict[str, str]:
        """提取指南章节结构（按 ## 标题分割）"""
        sections = {}
        pattern = r'## (.+?)\n(.*?)(?=\n## |\Z)'
        for match in re.finditer(pattern, text, re.DOTALL):
            title = match.group(1).strip()
            content = match.group(2).strip()
            sections[title] = content
        return sections
