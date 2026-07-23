"""
SQLAlchemy ORM 数据模型（与 MySQL 表对应）
"""
from datetime import datetime
from sqlalchemy import Column, Integer, String, Text, DateTime, Float, JSON
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
