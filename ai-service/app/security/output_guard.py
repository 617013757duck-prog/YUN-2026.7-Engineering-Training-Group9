"""
AI 输出安全审查器
检测：越权诊断、处方建议、危险建议、缺少安全声明
"""
import re
from typing import List, Dict
from dataclasses import dataclass, field


@dataclass
class OutputGuardResult:
    passed: bool
    violations: List[Dict] = field(default_factory=list)
    sanitized_output: str = ""
    risk_level: str = "safe"


class OutputGuard:
    """
    AI 输出安全审查器
    检测的违规类型：
    1. 越权诊断（确诊、病症断言）
    2. 处方建议（开药、剂量、用法）
    3. 治疗建议替代诊断
    4. 危险建议（自行用药、拒绝就医）
    """

    # 越权诊断模式
    DIAGNOSIS_PATTERNS = [
        (r'确诊为\s*[\u4e00-\u9fa5]{2,10}(病|症|炎|瘤|癌)', "越权诊断-确诊断言"),
        (r'你患[有了]\s*[\u4e00-\u9fa5]{2,10}', "越权诊断-患病断言"),
        (r'根据.{0,10}(症状|描述|情况).{0,10}判断.{0,5}你.{0,10}(是|得了|患有)', "越权诊断-推理诊断"),
        (r'诊断[结果]?\s*[是为：:]\s*[\u4e00-\u9fa5]{2,10}', "越权诊断-诊断结论"),
    ]

    # 处方建议模式
    PRESCRIPTION_PATTERNS = [
        (r'开[药方]\s*[\u4e00-\u9fa5()（）\d\w]{2,30}', "越权处方-开药建议"),
        (r'(服用|口服|注射|外用)\s*[\u4e00-\u9fa5()（）\d\w]{2,30}(mg|g|ml|片|粒|次|天|日|周)', "越权处方-剂量建议"),
        (r'(建议|推荐|可以)\s*(使用|服用|吃).{0,10}(药|片|胶囊|颗粒|注射)', "越权处方-用药建议"),
        (r'处方\s*[：:]\s*', "越权处方-处方格式"),
    ]

    # 危险建议模式
    DANGEROUS_ADVICE_PATTERNS = [
        (r'不[必用需].{0,5}(就医|看医生|去医院)', "危险建议-劝阻就医"),
        (r'自行.{0,5}(用药|治疗|处理|解决)', "危险建议-自行治疗"),
        (r'这个.*不严重.*不用.*(管|处理|担心)', "危险建议-轻描淡写"),
        (r'停止.{0,5}(用药|治疗|就医)', "危险建议-中断治疗"),
    ]

    def check(self, ai_output: str) -> OutputGuardResult:
        """审查 AI 输出"""
        violations = []

        # 检查1: 越权诊断
        for pattern, violation_type in self.DIAGNOSIS_PATTERNS:
            matches = re.findall(pattern, ai_output)
            for m in matches:
                violations.append({
                    "type": violation_type,
                    "matched_text": m if isinstance(m, str) else m[0],
                })

        # 检查2: 处方建议
        for pattern, violation_type in self.PRESCRIPTION_PATTERNS:
            matches = re.findall(pattern, ai_output)
            for m in matches:
                violations.append({
                    "type": violation_type,
                    "matched_text": m if isinstance(m, str) else m[0],
                })

        # 检查3: 危险建议
        for pattern, violation_type in self.DANGEROUS_ADVICE_PATTERNS:
            matches = re.findall(pattern, ai_output)
            for m in matches:
                violations.append({
                    "type": violation_type,
                    "matched_text": m if isinstance(m, str) else m[0],
                })

        # 检查4: 缺少安全声明
        if "不构成医疗" not in ai_output and "教学模拟" not in ai_output:
            violations.append({
                "type": "缺少安全声明",
                "matched_text": "",
            })

        passed = len(violations) == 0
        risk_level = "dangerous" if violations else "safe"

        return OutputGuardResult(
            passed=passed,
            violations=violations,
            sanitized_output=self._sanitize(ai_output, violations),
            risk_level=risk_level,
        )

    def _sanitize(self, text: str, violations: List[Dict]) -> str:
        """清理违规内容"""
        for v in violations:
            if v["matched_text"]:
                text = text.replace(
                    v["matched_text"],
                    f"[系统拦截-{v['type']}]",
                )

        # 追加安全声明
        if not text.endswith("不构成医疗建议"):
            text += "\n\n---\n【系统声明】以上内容为AI教学模拟生成，不构成医疗诊断或处方建议。如有身体不适，请及时到正规医疗机构就诊。"
        return text
