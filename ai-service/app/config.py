"""
AI 服务配置文件
"""
import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 服务
    APP_NAME: str = "Medical AI Service"
    APP_VERSION: str = "1.0.0"
    HOST: str = "0.0.0.0"
    PORT: int = 8000

    # MySQL（同步）
    MYSQL_HOST: str = "localhost"
    MYSQL_PORT: int = 3306
    MYSQL_USER: str = "root"
    MYSQL_PASSWORD: str = "root"
    MYSQL_DATABASE: str = "medical_platform"

    # 模型
    EMBEDDING_MODEL_PATH: str = "BAAI/bge-large-zh-v1.5"  # 中文向量化大模型(~1.3GB，精度最高)
    EMBEDDING_DEVICE: str = "cpu"  # CPU 推理（无 GPU 环境）

    # DeepSeek API（LLM 推理统一使用 deepseek-v4-flash）
    DEEPSEEK_API_KEY: str = ""            # 从环境变量注入
    DEEPSEEK_API_URL: str = "https://api.deepseek.com/v1"
    DEEPSEEK_MODEL: str = "deepseek-v4-flash"

    # FAISS
    FAISS_INDEX_PATH: str = "knowledge_base/faiss_index"
    TOP_K_RETRIEVAL: int = 5

    # 安全
    MAX_INPUT_LENGTH: int = 2000

    class Config:
        env_file = ".env"
        extra = "ignore"  # 忽略 .env 中未定义的字段（如 HF_ENDPOINT）


settings = Settings()
