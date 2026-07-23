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
