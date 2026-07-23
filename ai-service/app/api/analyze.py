"""
AI 分析 API
"""
from fastapi import APIRouter
from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
from app.config import settings

router = APIRouter()


class AnalyzeRequest(BaseModel):
    """分析请求"""
    visit_id: int
    symptoms: List[str]
    patient_age: int
    duration_days: int
    additional_info: Optional[str] = None


class AnalyzeResponse(BaseModel):
    """分析响应"""
    visit_id: int
    risk_level: str
    analysis: str
    guideline_refs: List[dict]
    suggestions: List[str]
    model_version: str
    prompt_version: str
    kb_version: str
    timestamp: str


@router.post("/analyze", response_model=AnalyzeResponse)
async def analyze_symptoms(request: AnalyzeRequest):
    """
    症状分析接口

    TODO: 实现完整的多 Agent 分析流程（Day2-Day3）
    1. 症状向量化 → RAG 检索
    2. 多 Agent 危险复核
    3. 安全护栏检查
    """
    # 当前为占位实现，后续 Day2/Day3 接入真实逻辑
    return AnalyzeResponse(
        visit_id=request.visit_id,
        risk_level="中风险",
        analysis="症状分析结果（待实现 - Day2/Day3 接入多 Agent 流程）",
        guideline_refs=[
            {"title": "预问诊指南", "section": "常见症状"},
        ],
        suggestions=["建议休息观察", "若症状加重请及时就医"],
        model_version=settings.DEEPSEEK_MODEL,
        prompt_version="v1.0.0",
        kb_version="2026.07",
        timestamp=datetime.now().isoformat(),
    )
