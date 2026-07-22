"""
AI 服务配置文件
"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 服务配置
    SERVICE_NAME: str = "Medical AI Service"
    SERVICE_VERSION: str = "1.0.0"
    SERVICE_HOST: str = "0.0.0.0"
    SERVICE_PORT: int = 8000
    
    # 模型配置
    MODEL_NAME: str = "THUDM/chatglm3-6b"
    MODEL_PATH: str = "./models"
    EMBEDDING_MODEL: str = "BAAI/bge-large-zh-v1.5"
    
    # 向量数据库配置
    VECTOR_DB_PATH: str = "./data/faiss_index"
    
    # 知识库配置
    KNOWLEDGE_BASE_PATH: str = "./knowledge_base"
    
    # 数据库配置
    DATABASE_URL: str = "mysql+pymysql://root:your_password@localhost:3306/medical_platform"
    
    # 安全配置
    MAX_TOKENS: int = 2048
    TEMPERATURE: float = 0.7
    
    class Config:
        env_file = ".env"


settings = Settings()