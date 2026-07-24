"""
Agent 3：建议生成 Agent
职责：生成随访建议、注意事项，绝对不输出诊断或处方
"""
import re
from typing import Dict, Any
from app.agents.base import BaseAgent


class SuggestionAgent(BaseAgent):
    """
    建议生成 Agent
    - 生成随访建议
    - 列出注意事项
    - 给出生活方式调整建议
    - 绝对不输出诊断或处方
    """

    # 禁止输出词汇（越权诊断/处方检测）
    FORBIDDEN_PATTERNS = [
        r'确诊为.{2,10}病',
        r'开[药方].{2,20}',
        r'服用.{2,10}(mg|g|片|粒|次)',
        r'你患[有了].{2,10}',
    ]

    def __init__(self, llm_service, prompt_manager):
        super().__init__(
            name="SuggestionAgent",
            version="1.0.0",
            llm_service=llm_service,
            prompt_manager=prompt_manager,
        )

    def _process(self, **kwargs) -> Dict[str, Any]:
        symptom_analysis = kwargs.get("symptom_analysis", {})
        risk_assessment = kwargs.get("risk_assessment", {})

        # LLM 生成建议（使用 deepseek-v4-flash API）
        prompt = self.prompt_manager.render(
            "suggestion_generation",
            symptom_analysis=str(symptom_analysis),
            risk_assessment=str(risk_assessment),
        )
        llm_output = self.llm_service.generate(prompt)

        # 后置审查：检测越权诊断
        violations = self._detect_violations(llm_output)
        if violations:
            llm_output = self._sanitize_output(llm_output, violations)

        return {
            "suggestions": llm_output,
            "violations_detected": violations,
            "risk_level": risk_assessment.get("risk_level", "未知"),
            "is_sanitized": len(violations) > 0,
        }

    def _detect_violations(self, text: str) -> list:
        """检测越权诊断内容"""
        violations = []
        for pattern in self.FORBIDDEN_PATTERNS:
            matches = re.findall(pattern, text)
            for m in matches:
                violations.append({
                    "pattern": pattern,
                    "matched": m,
                    "type": "unauthorized_diagnosis",
                })
        return violations

    def _sanitize_output(self, text: str, violations: list) -> str:
        """清理越权输出，替换为安全声明"""
        for v in violations:
            text = re.sub(
                re.escape(v["matched"]),
                "[此处内容因涉及越权诊断已被系统拦截]",
                text,
            )
        text += "\n\n【系统声明】以上建议为教学模拟内容，不构成医疗诊断或处方建议。如有不适请及时就医。"
        return text
