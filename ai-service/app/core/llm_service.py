"""
DeepSeek API 推理服务（统一 LLM 入口，使用 deepseek-v4-flash 模型）
"""
from typing import Optional, Generator
from openai import OpenAI
from app.config import settings


class DeepSeekAPIService:
    """DeepSeek API 推理服务（统一 LLM 入口，使用 deepseek-v4-flash 模型）"""

    def __init__(self):
        self.client = OpenAI(
            api_key=settings.DEEPSEEK_API_KEY,
            base_url=settings.DEEPSEEK_API_URL,
        )
        self.model = settings.DEEPSEEK_MODEL  # "deepseek-v4-flash"

    def generate(
        self,
        prompt: str,
        max_tokens: int = 2048,
        temperature: float = 0.1,
        top_p: float = 0.7,
        system_prompt: Optional[str] = None,
    ) -> str:
        """同步推理（非流式）"""
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        response = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            max_tokens=max_tokens,
            temperature=temperature,
            top_p=top_p,
        )
        return response.choices[0].message.content

    def stream_generate(
        self,
        prompt: str,
        max_tokens: int = 2048,
        temperature: float = 0.1,
        system_prompt: Optional[str] = None,
    ) -> Generator[str, None, None]:
        """流式推理"""
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        stream = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            max_tokens=max_tokens,
            temperature=temperature,
            stream=True,
        )
        for chunk in stream:
            if chunk.choices[0].delta.content:
                yield chunk.choices[0].delta.content

    def generate_with_system_prompt(
        self,
        system_prompt: str,
        user_content: str,
        **kwargs,
    ) -> str:
        """便捷方法：带 system prompt 的推理"""
        return self.generate(
            prompt=user_content,
            system_prompt=system_prompt,
            **kwargs,
        )
