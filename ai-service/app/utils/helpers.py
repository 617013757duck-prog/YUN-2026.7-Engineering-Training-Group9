"""
通用工具函数
"""
import hashlib
import time
from typing import Any, Callable
from loguru import logger


def sha256_hash(text: str) -> str:
    """计算文本的 SHA256 哈希"""
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def generate_chunk_id(source: str, index: int) -> str:
    """生成唯一的文档片段 ID"""
    raw = f"{source}_{index}_{time.time_ns()}"
    return hashlib.md5(raw.encode("utf-8")).hexdigest()[:16]


def timing(func: Callable) -> Callable:
    """装饰器：记录函数执行时间"""
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = func(*args, **kwargs)
        elapsed = (time.perf_counter() - start) * 1000
        logger.debug(f"{func.__name__} 执行耗时: {elapsed:.2f}ms")
        return result
    return wrapper


def safe_json_dumps(obj: Any, default: str = "{}") -> str:
    """安全的 JSON 序列化"""
    import json
    try:
        return json.dumps(obj, ensure_ascii=False, default=str)
    except Exception:
        return default
