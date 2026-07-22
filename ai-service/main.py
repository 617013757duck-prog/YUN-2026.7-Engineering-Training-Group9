"""
医疗 AI 服务 - 主入口
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from app.config import settings
from app.api import analyze, health

# 创建 FastAPI 应用
app = FastAPI(
    title=settings.SERVICE_NAME,
    version=settings.SERVICE_VERSION,
    description="基层医疗安全型预问诊与随访平台 - AI 服务"
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
app.include_router(health.router, prefix="/api", tags=["健康检查"])
app.include_router(analyze.router, prefix="/api/ai", tags=["AI 分析"])


@app.on_event("startup")
async def startup_event():
    """服务启动时执行"""
    logger.info(f"AI 服务启动: {settings.SERVICE_NAME} v{settings.SERVICE_VERSION}")
    logger.info(f"模型路径: {settings.MODEL_PATH}")
    logger.info(f"向量数据库路径: {settings.VECTOR_DB_PATH}")


@app.on_event("shutdown")
async def shutdown_event():
    """服务关闭时执行"""
    logger.info("AI 服务关闭")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.SERVICE_HOST,
        port=settings.SERVICE_PORT,
        reload=True
    )