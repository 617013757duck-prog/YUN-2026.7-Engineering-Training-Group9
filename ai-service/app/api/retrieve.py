"""
RAG 知识检索 API
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional
from app.knowledge_base.retriever import MedicalRetriever

router = APIRouter()
# 延迟初始化，避免启动时立即下载模型
_retriever = None


def get_retriever():
    """获取检索器实例（延迟初始化）"""
    global _retriever
    if _retriever is None:
        _retriever = MedicalRetriever()
    return _retriever


class RetrieveRequest(BaseModel):
    query: str = Field(..., description="用户症状描述", max_length=2000)
    visit_id: int = Field(..., description="就诊记录ID")
    top_k: Optional[int] = Field(5, description="返回结果数量")


class CitationItem(BaseModel):
    chunk_id: str
    source: str
    section: str
    content: str
    score: float


class RetrieveResponse(BaseModel):
    success: bool
    query: str
    results: List[CitationItem]
    context: str  # 拼接好的上下文，供 LLM 使用
    total: int


@router.post("/retrieve", response_model=RetrieveResponse)
async def retrieve_knowledge(req: RetrieveRequest):
    """RAG 知识检索接口"""
    try:
        retriever = get_retriever()
        results = retriever.retrieve(
            query=req.query,
            visit_id=req.visit_id,
            top_k=req.top_k,
        )
        context = retriever.format_context(results)
        return RetrieveResponse(
            success=True,
            query=req.query,
            results=results,
            context=context,
            total=len(results),
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检索失败: {str(e)}")
