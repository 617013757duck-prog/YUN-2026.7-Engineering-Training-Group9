"""
隐私保护模块
检测和脱敏：身份证号、手机号、姓名、地址、银行卡号、邮箱
"""
import re
from typing import List, Dict
from dataclasses import dataclass, field


@dataclass
class PrivacyResult:
    has_sensitive: bool
    sensitive_items: List[Dict] = field(default_factory=list)
    desensitized_text: str = ""


class PrivacyGuard:
    """
    隐私保护模块
    检测和脱敏类型：
    1. 身份证号
    2. 手机号
    3. 固定电话
    4. 银行卡号
    5. 电子邮箱
    6. 家庭地址
    """

    # 敏感信息正则（按优先级排序，避免误匹配）
    PATTERNS = {
        "身份证号": (r'\b[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]\b', '***身份证号***'),
        "手机号": (r'\b1[3-9]\d{9}\b', '***手机号***'),
        "固定电话": (r'\b0\d{2,3}[-\s]?\d{7,8}\b', '***座机号***'),
        "银行卡号": (r'\b\d{16,19}\b', '***银行卡号***'),
        "电子邮箱": (r'\b[\w.\-]+@[\w.\-]+\.\w+\b', '***邮箱***'),
        "家庭地址": (r'(省|市|区|县|镇|路|街|号|栋|单元|室).{3,20}(号|室)', '***地址***'),
    }

    def check_and_desensitize(self, text: str) -> 'PrivacyResult':
        """检测并脱敏"""
        sensitive_items = []
        result_text = text

        for name, (pattern, replacement) in self.PATTERNS.items():
            matches = re.findall(pattern, result_text)
            if matches:
                for m in matches:
                    sensitive_items.append({
                        "type": name,
                        "matched": m if isinstance(m, str) else m[0],
                    })
                result_text = re.sub(pattern, replacement, result_text)

        return PrivacyResult(
            has_sensitive=len(sensitive_items) > 0,
            sensitive_items=sensitive_items,
            desensitized_text=result_text,
        )
