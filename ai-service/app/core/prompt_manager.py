"""
Prompt 模板管理器 —— 按场景分类管理
"""
from typing import Dict, List
from dataclasses import dataclass, field


@dataclass
class PromptTemplate:
    name: str
    description: str
    system_prompt: str
    user_template: str
    variables: List[str] = field(default_factory=list)
    version: str = "1.0.0"


class PromptManager:
    """Prompt 模板管理器 —— 按场景分类管理"""

    def __init__(self):
        self.templates: Dict[str, PromptTemplate] = {}
        self._register_defaults()

    def _register_defaults(self):
        """注册默认 Prompt 模板"""

        # 场景1：症状分析
        self.register(PromptTemplate(
            name="symptom_analysis",
            description="分析用户症状，提取关键症状并量化严重程度",
            system_prompt="""你是一个医疗教学辅助系统的症状分析模块。你的职责是：
1. 从用户描述中提取关键症状
2. 量化症状的严重程度（轻度/中度/重度）
3. 标注症状持续时间
4. 识别可能的红旗症状（需要立即就医的警示信号）

重要约束：
- 你不提供诊断，仅做症状结构化分析
- 你必须声明"本分析为教学模拟，不构成医疗建议"
- 如果信息不足，明确标注"信息不足，建议补充" """,
            user_template="""用户症状描述：
{user_symptoms}

请进行症状分析。""",
            variables=["user_symptoms"],
            version="1.0.0",
        ))

        # 场景2：风险判断
        self.register(PromptTemplate(
            name="risk_assessment",
            description="基于指南和症状分析进行风险判断",
            system_prompt="""你是一个医疗教学辅助系统的风险判断模块。你的职责是：
1. 基于提供的医疗指南片段，评估用户的健康风险
2. 给出风险等级（低/中/高/紧急）
3. 说明风险判断的依据（引用指南来源）

重要约束：
- 你不提供诊断，仅进行风险评估
- 必须引用指南来源作为判断依据
- 你必须声明"本评估为教学模拟，不构成医疗建议" """,
            user_template="""症状分析结果：
{symptom_analysis}

参考指南：
{context}

请进行风险评估。""",
            variables=["symptom_analysis", "context"],
            version="1.0.0",
        ))

        # 场景3：建议生成
        self.register(PromptTemplate(
            name="suggestion_generation",
            description="生成随访建议和注意事项",
            system_prompt="""你是一个医疗教学辅助系统的建议生成模块。你的职责是：
1. 基于症状分析和风险评估，生成随访建议
2. 列出需要关注的注意事项
3. 给出生活方式调整建议

重要约束：
- 你不开具处方或推荐具体药物
- 你必须声明"本建议为教学模拟，不构成医疗建议"
- 如果风险等级为"紧急"，建议立即就医""",
            user_template="""症状分析结果：
{symptom_analysis}

风险评估结果：
{risk_assessment}

请生成随访建议。""",
            variables=["symptom_analysis", "risk_assessment"],
            version="1.0.0",
        ))

        # 场景4：随访提醒生成
        self.register(PromptTemplate(
            name="followup_reminder",
            description="生成慢病随访提醒内容",
            system_prompt="""你是一个医疗教学辅助系统的随访提醒模块。你的职责是：
1. 根据随访计划生成个性化的提醒内容
2. 包含随访检查项目和目标值
3. 语气温和但有督促作用

重要约束：
- 不包含具体用药剂量
- 声明"本提醒为教学模拟" """,
            user_template="""随访计划信息：
{followup_plan}

请生成随访提醒内容。""",
            variables=["followup_plan"],
            version="1.0.0",
        ))

    def register(self, template: PromptTemplate):
        """注册模板"""
        self.templates[template.name] = template

    def get(self, name: str) -> PromptTemplate:
        """获取模板"""
        if name not in self.templates:
            raise ValueError(f"Prompt 模板 '{name}' 不存在")
        return self.templates[name]

    def render(self, name: str, **kwargs) -> str:
        """渲染模板（填充变量）"""
        template = self.get(name)
        try:
            user_prompt = template.user_template.format(**kwargs)
        except KeyError as e:
            raise ValueError(f"缺少变量 {e}，模板 '{name}' 需要：{template.variables}")

        full_prompt = f"{template.system_prompt}\n\n{user_prompt}"
        return full_prompt

    def list_templates(self) -> List[Dict]:
        """列出所有模板"""
        return [
            {
                "name": t.name,
                "description": t.description,
                "version": t.version,
                "variables": t.variables,
            }
            for t in self.templates.values()
        ]
