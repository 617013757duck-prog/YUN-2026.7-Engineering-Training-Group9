"""
Agent 1：症状分析 Agent
职责：提取关键症状、量化严重程度、识别红旗症状
"""
import re
from typing import Dict, Any
from app.agents.base import BaseAgent


class SymptomAnalysisAgent(BaseAgent):
    """
    症状分析 Agent
    - 从用户描述中提取关键症状
    - 量化症状严重程度（轻度/中度/重度）
    - 标注持续时间
    - 识别红旗症状（需要立即就医的警示信号）
    """

    # 红旗症状关键词库（可扩展）
    RED_FLAG_KEYWORDS = [
        "胸痛", "呼吸困难", "咳血", "剧烈头痛", "意识模糊",
        "偏瘫", "言语不清", "高热不退", "抽搐", "昏迷",
        "呕血", "黑便", "剧烈腹痛", "视力突然下降",
    ]

    # 严重程度关键词映射
    SEVERITY_KEYWORDS = {
        "重度": ["剧烈", "严重", "无法忍受", "持续不断", "极度"],
        "中度": ["明显", "反复", "影响", "频繁", "不适"],
        "轻度": ["偶尔", "轻微", "略有", "稍感", "基本正常"],
    }

    def __init__(self, llm_service, prompt_manager):
        super().__init__(
            name="SymptomAnalysisAgent",
            version="1.0.0",
            llm_service=llm_service,
            prompt_manager=prompt_manager,
        )

    def _process(self, **kwargs) -> Dict[str, Any]:
        user_symptoms = kwargs.get("user_symptoms", "")

        # 规则预筛：检测红旗症状
        red_flags = self._detect_red_flags(user_symptoms)
        severity_hint = self._estimate_severity(user_symptoms)

        # LLM 深度分析（使用 deepseek-v4-flash API）
        prompt = self.prompt_manager.render(
            "symptom_analysis",
            user_symptoms=user_symptoms,
        )
        llm_output = self.llm_service.generate(prompt)

        # 结构化解析 LLM 输出
        structured = self._parse_llm_output(llm_output)

        return {
            "user_symptoms": user_symptoms,
            "red_flags": red_flags,
            "severity_hint": severity_hint,
            "structured_symptoms": structured.get("symptoms", []),
            "raw_llm_output": llm_output,
            "has_red_flag": len(red_flags) > 0,
        }

    def _detect_red_flags(self, text: str) -> list:
        """基于关键词检测红旗症状"""
        found = []
        for kw in self.RED_FLAG_KEYWORDS:
            if kw in text:
                found.append({"symptom": kw, "type": "red_flag"})
        return found

    def _estimate_severity(self, text: str) -> str:
        """基于关键词估计严重程度"""
        scores = {"重度": 0, "中度": 0, "轻度": 0}
        for level, keywords in self.SEVERITY_KEYWORDS.items():
            for kw in keywords:
                if kw in text:
                    scores[level] += 1
        return max(scores, key=scores.get) if any(scores.values()) else "未知"

    def _parse_llm_output(self, output: str) -> Dict:
        """解析 LLM 输出为结构化数据"""
        symptoms = []
        for line in output.split("\n"):
            line = line.strip()
            if "：" in line or ":" in line:
                parts = re.split(r'[：:]', line, maxsplit=1)
                if len(parts) == 2:
                    symptoms.append({
                        "name": parts[0].strip().lstrip("-*0123456789. "),
                        "description": parts[1].strip(),
                    })
        return {"symptoms": symptoms}
