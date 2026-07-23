"""
Agent 2：风险判断 Agent
职责：基于症状分析 + 医疗指南 RAG 检索，评估健康风险等级
"""
import re
from typing import Dict, Any
from app.agents.base import BaseAgent


class RiskAssessmentAgent(BaseAgent):
    """
    风险判断 Agent
    - 基于症状分析结果与医疗指南，评估健康风险
    - 输出风险等级（低/中/高/紧急）
    - 引用指南来源作为判断依据
    - 检查禁忌条件
    """

    # 风险等级排序（用于取最高风险）
    RISK_ORDER = {"低": 1, "中": 2, "高": 3, "紧急": 4}

    # 紧急情况关键词
    EMERGENCY_KEYWORDS = [
        "立即就医", "急救", "生命危险", "危急", "猝死风险",
        "急性心肌梗死", "脑卒中", "主动脉夹层",
    ]

    def __init__(self, llm_service, prompt_manager, retriever):
        super().__init__(
            name="RiskAssessmentAgent",
            version="1.0.0",
            llm_service=llm_service,
            prompt_manager=prompt_manager,
        )
        self.retriever = retriever

    def _process(self, **kwargs) -> Dict[str, Any]:
        symptom_analysis = kwargs.get("symptom_analysis", {})
        user_symptoms = symptom_analysis.get("user_symptoms", "")

        # 规则预筛：红旗症状 → 自动提级
        has_red_flag = symptom_analysis.get("has_red_flag", False)

        # 规则预筛：紧急情况检测
        is_emergency = self._check_emergency(user_symptoms)

        # RAG 检索相关知识
        context = kwargs.get("context", "")
        if not context:
            results = self.retriever.retrieve(
                query=user_symptoms,
                visit_id=kwargs.get("visit_id", 0),
                top_k=5,
            )
            context = self.retriever.format_context(results)

        # LLM 风险评估（使用 deepseek-v4-flash API）
        prompt = self.prompt_manager.render(
            "risk_assessment",
            symptom_analysis=str(symptom_analysis),
            context=context,
        )
        llm_output = self.llm_service.generate(prompt)

        # 风险等级解析
        risk_level = self._parse_risk_level(llm_output)
        if is_emergency:
            risk_level = "紧急"
        # 红旗症状自动提级到"高"（除非已是"紧急"）
        if has_red_flag and risk_level not in ("紧急", "高"):
            risk_level = "高"

        # 提取引用
        citations = self._extract_citations(llm_output)

        return {
            "risk_level": risk_level,
            "is_emergency": is_emergency,
            "has_red_flag": has_red_flag,
            "raw_llm_output": llm_output,
            "citations": citations,
            "context_used": context[:500],
        }

    def _check_emergency(self, text: str) -> bool:
        """检测紧急情况"""
        return any(kw in text for kw in self.EMERGENCY_KEYWORDS)

    def _parse_risk_level(self, output: str) -> str:
        """从 LLM 输出中解析风险等级"""
        for level in ["紧急", "高", "中", "低"]:
            if level in output:
                return level
        return "中"  # 默认中等风险

    def _extract_citations(self, output: str) -> list:
        """提取输出中的引用标记 [参考N]"""
        citations = []
        pattern = r'\[参考(\d+)\]\s*(.+?)(?=\[参考|\Z)'
        for match in re.finditer(pattern, output, re.DOTALL):
            citations.append({
                "ref_id": match.group(1),
                "content": match.group(2).strip()[:200],
            })
        return citations
