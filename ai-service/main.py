"""
医疗 AI 服务 - 主入口
"""
# 必须在所有导入之前设置 HuggingFace 镜像（解决国内网络问题）
import os
from dotenv import load_dotenv
load_dotenv()
if os.getenv("HF_ENDPOINT"):
    os.environ.setdefault("HF_ENDPOINT", os.getenv("HF_ENDPOINT"))

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from app.config import settings
from app.core.logging_config import setup_logging
from app.api import health, analyze
from app.api.retrieve import router as retrieve_router
from app.api.llm import router as llm_router
from app.api.agents import router as agents_router
from app.api.safety import router as safety_router
from app.api.followup import router as followup_router
from app.api.version import router as version_router

# 初始化日志
setup_logging()

# 创建 FastAPI 应用
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="基层医疗安全型预问诊与随访平台 - AI 服务",
)

# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(health.router, prefix="/api/ai", tags=["Health"])
app.include_router(analyze.router, prefix="/api/ai", tags=["Analyze"])
app.include_router(retrieve_router, prefix="/api/ai", tags=["Retrieve"])
app.include_router(llm_router, prefix="/api/ai", tags=["LLM"])
app.include_router(agents_router, prefix="/api/ai", tags=["Agents"])
app.include_router(safety_router, prefix="/api/ai", tags=["Safety"])
app.include_router(followup_router, prefix="/api/ai", tags=["Followup"])
app.include_router(version_router, prefix="/api/ai", tags=["Version"])


@app.on_event("startup")
async def startup_event():
    """服务启动时执行"""
    logger.info(f"AI 服务启动: {settings.APP_NAME} v{settings.APP_VERSION}")

    # 初始化向量库
    try:
        from app.knowledge_base.vector_store import VectorStore
        VectorStore.load_index()
        logger.info("FAISS 向量库加载完成")
    except Exception as e:
        logger.warning(f"FAISS 向量库加载失败（首次启动可能索引不存在）: {e}")

    # 初始化多 Agent 系统（LLM 统一使用 deepseek-v4-flash API）
    try:
        from app.core.llm_service import DeepSeekAPIService
        from app.core.prompt_manager import PromptManager
        from app.knowledge_base.retriever import MedicalRetriever
        from app.api.agents import init_orchestrator

        llm_service = DeepSeekAPIService()
        prompt_manager = PromptManager()
        retriever = MedicalRetriever()
        init_orchestrator(llm_service, prompt_manager, retriever)
        logger.info("多 Agent 系统初始化完成")
    except Exception as e:
        logger.warning(f"多 Agent 系统初始化失败（API Key 可能未配置）: {e}")

    # 注册模型版本（LLM 统一使用 deepseek-v4-flash API）
    try:
        from app.services.version_service import VersionService
        vs = VersionService()
        existing = vs.get_active_model()
        if not existing:
            vs.register_model(
                model_name="deepseek-v4-flash",
                version="1.0.0",
                api_endpoint=settings.DEEPSEEK_API_URL,
                model_id="deepseek-v4-flash",
                prompt_version="v2.0",
            )
            logger.info("模型版本已注册: deepseek-v4-flash v1.0.0")
        else:
            logger.info(f"当前模型版本: {existing['model_name']} v{existing['version']}")
    except Exception as e:
        logger.warning(f"模型版本注册失败（数据库可能未连接）: {e}")


@app.on_event("shutdown")
async def shutdown_event():
    """服务关闭时执行"""
    logger.info("AI 服务关闭")


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
    )
