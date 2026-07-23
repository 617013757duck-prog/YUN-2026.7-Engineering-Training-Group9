"""
SQLAlchemy ORM 数据模型（与 MySQL 表对应）
"""
from datetime import datetime
from sqlalchemy import Column, Integer, String, Text, DateTime, Float, JSON, Boolean
from sqlalchemy.orm import declarative_base

Base = declarative_base()


class MedicalGuideline(Base):
    """医疗指南记录表"""
    __tablename__ = "medical_guidelines"

    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(200), nullable=False)
    source = Column(String(200))
    version = Column(String(50))
    year = Column(Integer)
    raw_content = Column(Text)
    chunk_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.now)


class Citation(Base):
    """引用记录表"""
    __tablename__ = "citations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    visit_id = Column(Integer, nullable=False)
    chunk_id = Column(String(100))
    source = Column(String(200))
    section = Column(String(200))
    content = Column(Text)
    score = Column(Float)
    created_at = Column(DateTime, default=datetime.now)


class ModelVersion(Base):
    """模型版本记录表（LLM 统一使用 deepseek-v4-flash API）"""
    __tablename__ = "model_versions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    model_name = Column(String(100), nullable=False)      # e.g. "deepseek-v4-flash"
    api_endpoint = Column(String(500))                     # API 端点地址
    model_id = Column(String(100))                         # 模型标识 e.g. "deepseek-v4-flash"
    version = Column(String(50), nullable=False)           # e.g. "1.0.0"
    prompt_version = Column(String(50))                    # Prompt 模板版本
    description = Column(String(500))
    is_active = Column(Boolean, default=False)             # 是否当前使用
    deployed_at = Column(DateTime)
    created_at = Column(DateTime)


class KnowledgeBaseSnapshot(Base):
    """知识库版本快照表"""
    __tablename__ = "knowledge_base_snapshots"

    id = Column(Integer, primary_key=True, autoincrement=True)
    version = Column(String(50), nullable=False)
    total_chunks = Column(Integer)                         # 文档片段数
    total_vectors = Column(Integer)                        # 向量数
    embedding_model = Column(String(100))                  # e.g. "BAAI/bge-large-zh-v1.5"
    chunk_size = Column(Integer)                           # 切分大小
    sources = Column(JSON)                                 # 指南来源列表
    faiss_index_hash = Column(String(64))                  # 索引 SHA256 哈希
    description = Column(String(500))
    created_at = Column(DateTime)


class AgentRunRecord(Base):
    """Agent 执行完整调用链记录表"""
    __tablename__ = "agent_runs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    visit_id = Column(Integer, nullable=False)
    run_id = Column(String(50), unique=True, nullable=False)
    model_version_id = Column(Integer)
    kb_snapshot_id = Column(Integer)
    prompt_version = Column(String(50))
    user_input_raw = Column(Text)                          # 原始用户输入
    user_input_desensitized = Column(Text)                 # 脱敏后输入
    retriever_results = Column(JSON)                       # RAG 检索结果
    agent_1_output = Column(JSON)                          # Agent 1 输出
    agent_2_output = Column(JSON)                          # Agent 2 输出
    agent_3_output = Column(JSON)                          # Agent 3 输出
    ai_output_raw = Column(Text)                           # AI 原始输出
    ai_output_sanitized = Column(Text)                     # 审查后输出
    safety_check_passed = Column(Boolean)
    safety_violations = Column(JSON)
    total_duration_ms = Column(Float)
    input_tokens = Column(Integer)
    output_tokens = Column(Integer)
    created_at = Column(DateTime)
