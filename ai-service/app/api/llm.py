"""
LLM 推理 API（deepseek-v4-flash）
"""
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field
from typing import Optional
from app.core.llm_service import DeepSeekAPIService
from app.core.prompt_manager import PromptManager

router = APIRouter()
prompt_manager = PromptManager()
llm_service = DeepSeekAPIService()  # deepseek-v4-flash API 服务


class LLMRequest(BaseModel):
    prompt_template: str = Field(..., description="Prompt 模板名称")
    variables: dict = Field(default_factory=dict, description="模板变量值")
    max_tokens: Optional[int] = Field(2048, description="最大生成 token 数")
    temperature: Optional[float] = Field(0.1, ge=0.0, le=1.0, description="温度参数")
    stream: bool = Field(False, description="是否流式输出")


class LLMResponse(BaseModel):
    success: bool
    template_used: str
    template_version: str
    generated_text: str
    model: str  # "deepseek-v4-flash"


@router.post("/generate", response_model=LLMResponse)
async def generate_text(req: LLMRequest):
    """LLM 推理接口（deepseek-v4-flash）"""
    try:
        template = prompt_manager.get(req.prompt_template)
        prompt = prompt_manager.render(req.prompt_template, **req.variables)

        result = llm_service.generate(
            prompt=prompt,
            max_tokens=req.max_tokens,
            temperature=req.temperature,
        )

        return LLMResponse(
            success=True,
            template_used=template.name,
            template_version=template.version,
            generated_text=result,
            model=llm_service.model,
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"推理失败: {str(e)}")


@router.post("/generate/stream")
async def generate_text_stream(req: LLMRequest):
    """LLM 流式推理接口（deepseek-v4-flash）"""
    try:
        prompt = prompt_manager.render(req.prompt_template, **req.variables)

        def stream():
            for chunk in llm_service.stream_generate(
                prompt=prompt,
                max_tokens=req.max_tokens,
                temperature=req.temperature,
            ):
                yield f"data: {chunk}\n\n"
            yield "data: [DONE]\n\n"

        return StreamingResponse(stream(), media_type="text/event-stream")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/templates")
async def list_templates():
    """列出所有 Prompt 模板"""
    return {
        "success": True,
        "templates": prompt_manager.list_templates(),
    }


@router.get("/model/info")
async def model_info():
    """获取当前模型信息"""
    return {
        "success": True,
        "model": "deepseek-v4-flash",
        "api_url": llm_service.client.base_url,
        "prompt_templates": len(prompt_manager.list_templates()),
    }
