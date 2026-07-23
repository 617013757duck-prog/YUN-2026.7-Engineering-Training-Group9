"""
随访计划 API
POST /api/ai/followup/plan  — 生成随访计划
POST /api/ai/followup/reminder  — 生成随访提醒
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from loguru import logger
from app.services.followup_service import FollowupService

router = APIRouter()

followup_service = FollowupService()


class FollowupPlanRequest(BaseModel):
    visit_id: int = Field(..., description="就诊记录ID")
    risk_level: str = Field(..., description="风险等级（低/中/高/紧急）")
    agent_output: str = Field(default="", description="Agent 分析输出（可选）")


class FollowupPlanResponse(BaseModel):
    success: bool
    plan: dict


class FollowupReminderRequest(BaseModel):
    plan: dict = Field(..., description="随访计划（由 /plan 接口返回）")


class FollowupReminderResponse(BaseModel):
    success: bool
    reminder: str


@router.post("/followup/plan", response_model=FollowupPlanResponse)
async def generate_followup_plan(req: FollowupPlanRequest):
    """
    生成随访计划
    根据风险等级自动确定随访频率和持续时间
    """
    try:
        plan = followup_service.generate_plan(
            visit_id=req.visit_id,
            risk_level=req.risk_level,
            agent_output=req.agent_output,
        )
        return FollowupPlanResponse(success=True, plan=plan)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"随访计划生成失败: {str(e)}")


@router.post("/followup/reminder", response_model=FollowupReminderResponse)
async def generate_followup_reminder(req: FollowupReminderRequest):
    """
    生成随访提醒内容（使用 deepseek-v4-flash API）
    """
    try:
        reminder = followup_service.generate_reminder(req.plan)
        return FollowupReminderResponse(success=True, reminder=reminder)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"随访提醒生成失败: {str(e)}")
