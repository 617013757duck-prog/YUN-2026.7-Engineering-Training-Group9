"""
AI 安全网关（门面模式 Facade）
统一调度三个安全模块：输入过滤 → 隐私脱敏 → 输出审查
"""
from dataclasses import dataclass
from loguru import logger
from app.security.input_guard import InputGuard, GuardResult
from app.security.output_guard import OutputGuard, OutputGuardResult
from app.security.privacy_guard import PrivacyGuard, PrivacyResult


@dataclass
class SafetyReport:
    input_passed: bool
    input_risk: str
    output_passed: bool
    output_risk: str
    has_privacy_issue: bool
    desensitized_input: str
    desensitized_output: str
    blocked: bool


class SafetyGateway:
    """AI 安全网关（门面模式，统一调度三个安全模块）"""

    def __init__(self):
        self.input_guard = InputGuard()
        self.output_guard = OutputGuard()
        self.privacy_guard = PrivacyGuard()

    def process(self, user_input: str, ai_output: str) -> SafetyReport:
        """
        完整安全处理流程：
        1. 输入过滤 → 拦截危险输入
        2. 隐私脱敏 → 移除敏感信息
        3. 输出审查 → 拦截越权输出
        """
        # 1. 输入过滤
        input_result = self.input_guard.check(user_input)
        if not input_result.passed:
            logger.warning(f"输入拦截: {input_result.blocked_reasons}")
            return self._block("输入安全检查未通过", input_result)

        # 2. 隐私脱敏
        privacy_result = self.privacy_guard.check_and_desensitize(
            input_result.sanitized_input
        )
        if privacy_result.has_sensitive:
            logger.info(f"隐私脱敏: {len(privacy_result.sensitive_items)} 项敏感信息")

        # 3. 输出审查
        output_result = self.output_guard.check(ai_output)
        if not output_result.passed:
            logger.warning(f"输出审查未通过: {len(output_result.violations)} 项违规")

        return SafetyReport(
            input_passed=input_result.passed,
            input_risk=input_result.risk_level,
            output_passed=output_result.passed,
            output_risk=output_result.risk_level,
            has_privacy_issue=privacy_result.has_sensitive,
            desensitized_input=privacy_result.desensitized_text,
            desensitized_output=output_result.sanitized_output,
            blocked=False,
        )

    def _block(self, reason: str, result: GuardResult) -> SafetyReport:
        return SafetyReport(
            input_passed=False, input_risk="dangerous",
            output_passed=False, output_risk="dangerous",
            has_privacy_issue=False,
            desensitized_input="", desensitized_output=reason,
            blocked=True,
        )
