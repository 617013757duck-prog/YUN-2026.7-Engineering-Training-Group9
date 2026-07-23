"""
随访计划生成与提醒服务
"""
from typing import List, Dict
from datetime import datetime, timedelta
from loguru import logger
from app.core.prompt_manager import PromptManager
# LLM 统一使用 deepseek-v4-flash API
from app.core.llm_service import DeepSeekAPIService


class FollowupService:
    """随访计划和提醒服务"""

    def generate_plan(self, visit_id: int, risk_level: str,
                      agent_output: str) -> Dict:
        """
        基于 Agent 分析结果生成随访计划
        风险等级决定随访频率：
        - 紧急：每天1次，持续7天
        - 高：每3天1次，持续30天
        - 中：每7天1次，持续90天
        - 低：每14天1次，持续180天
        """
        frequency_map = {
            "紧急": {"interval_days": 1, "duration_days": 7},
            "高": {"interval_days": 3, "duration_days": 30},
            "中": {"interval_days": 7, "duration_days": 90},
            "低": {"interval_days": 14, "duration_days": 180},
        }
        config = frequency_map.get(risk_level, {"interval_days": 7, "duration_days": 90})

        # 生成随访任务列表
        tasks = []
        current_date = datetime.now()
        task_count = config["duration_days"] // config["interval_days"]

        for i in range(task_count):
            due_date = current_date + timedelta(days=(i + 1) * config["interval_days"])
            tasks.append({
                "visit_id": visit_id,
                "task_index": i + 1,
                "due_date": due_date.strftime("%Y-%m-%d"),
                "status": "pending",
                "description": f"第{i+1}次随访 - 风险等级: {risk_level}",
            })

        logger.info(
            f"随访计划生成: visit_id={visit_id}, risk={risk_level}, "
            f"tasks={len(tasks)}, interval={config['interval_days']}天"
        )

        return {
            "visit_id": visit_id,
            "risk_level": risk_level,
            "interval_days": config["interval_days"],
            "duration_days": config["duration_days"],
            "total_tasks": len(tasks),
            "tasks": tasks,
        }

    def generate_reminder(self, followup_plan: Dict) -> str:
        """使用 LLM 生成个性化随访提醒（使用 deepseek-v4-flash API）"""
        llm_service = DeepSeekAPIService()
        pm = PromptManager()
        prompt = pm.render("followup_reminder", followup_plan=str(followup_plan))
        return llm_service.generate(prompt)
