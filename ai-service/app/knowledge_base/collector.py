"""
公开医疗指南收集器
"""
import os
import json
from typing import List, Dict
from app.config import settings


class GuidelineCollector:
    """公开医疗指南收集器"""

    def __init__(self):
        self.raw_dir = "data/raw"

    def collect_from_json(self, file_path: str) -> List[Dict]:
        """从 JSON 格式的指南数据中加载"""
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        return data

    def collect_from_txt(self, file_path: str) -> str:
        """从纯文本指南中加载"""
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read()

    def save_raw(self, filename: str, content: str):
        """保存原始数据"""
        os.makedirs(self.raw_dir, exist_ok=True)
        path = os.path.join(self.raw_dir, filename)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
