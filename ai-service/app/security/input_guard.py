"""
输入安全过滤器
检测：提示词注入、角色扮演绕过、越狱尝试、恶意代码注入
"""
import re
from typing import List
from dataclasses import dataclass, field


@dataclass
class GuardResult:
    passed: bool
    risk_level: str  # "safe" | "suspicious" | "dangerous"
    blocked_reasons: List[str] = field(default_factory=list)
    sanitized_input: str = ""


class InputGuard:
    """
    输入安全过滤器
    检测的威胁类型：
    1. 提示词注入（Prompt Injection）
    2. 角色扮演绕过
    3. 指令覆盖攻击
    4. 越狱尝试（Jailbreak）
    """

    # 提示词注入关键词
    INJECTION_PATTERNS = [
        r'忽略.{0,10}(之前|上述|上面|前面|所有).{0,10}(指令|规则|限制|约束)',
        r'(ignore|forget|disregard).{0,20}(previous|above|instruction|rule)',
        r'你.{0,5}(现在|从现在起|重新).{0,5}(扮演|角色|身份)',
        r'(作为|假装|假设你是一个|你现在是).{0,10}(医生|专家|主任|教授)',
        r'(系统|system)\s*(提示|prompt|指令|instruction)\s*[：:]\s*',
        r'\[INST\].*\[/INST\]',   # Llama 指令格式注入
        r'<\|.*\|>',               # ChatGLM 特殊 token 注入
        r'忽略.{0,5}安全|绕过.{0,5}限制|跳过.{0,5}审核',
    ]

    # 角色扮演绕过模式
    ROLEPLAY_PATTERNS = [
        r'你现在是.*医生',
        r'作为医学专家',
        r'你拥有.*处方权',
        r'假装你是.*主任医师',
        r'以.*医生.*身份',
    ]

    # 越狱关键词
    JAILBREAK_KEYWORDS = [
        'DAN模式', '开发者模式', '无限制模式', '越狱',
        'jailbreak', 'DAN mode', 'developer mode',
        '解除限制', '取消安全', '关闭过滤',
    ]

    def check(self, user_input: str) -> GuardResult:
        """
        检查输入安全性
        返回 GuardResult
        """
        blocked_reasons = []

        # 检查1: 长度限制
        if len(user_input) > 2000:
            blocked_reasons.append("输入长度超过限制(2000字符)")

        # 检查2: 提示词注入
        for pattern in self.INJECTION_PATTERNS:
            if re.search(pattern, user_input, re.IGNORECASE):
                blocked_reasons.append(f"检测到提示词注入模式: {pattern}")
                break

        # 检查3: 角色扮演绕过
        for pattern in self.ROLEPLAY_PATTERNS:
            if re.search(pattern, user_input, re.IGNORECASE):
                blocked_reasons.append(f"检测到角色扮演绕过: {pattern}")
                break

        # 检查4: 越狱尝试
        for kw in self.JAILBREAK_KEYWORDS:
            if kw.lower() in user_input.lower():
                blocked_reasons.append(f"检测到越狱关键词: {kw}")
                break

        # 检查5: 恶意代码注入
        if re.search(r'<script|<iframe|javascript:|onerror=', user_input, re.IGNORECASE):
            blocked_reasons.append("检测到代码注入尝试")

        # 结果判定
        if len(blocked_reasons) >= 2 or any("注入" in r for r in blocked_reasons):
            risk_level = "dangerous"
            passed = False
        elif len(blocked_reasons) == 1:
            risk_level = "suspicious"
            passed = True  # 可疑但仍放行（记录日志）
        else:
            risk_level = "safe"
            passed = True

        return GuardResult(
            passed=passed,
            risk_level=risk_level,
            blocked_reasons=blocked_reasons,
            sanitized_input=self._sanitize(user_input),
        )

    def _sanitize(self, text: str) -> str:
        """基础文本清洗"""
        # 移除特殊 Unicode 控制字符
        text = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f-\x9f]', '', text)
        # 移除零宽字符（可用于绕过）
        text = re.sub(r'[\u200b\u200c\u200d\u200e\u200f\ufeff]', '', text)
        return text.strip()
