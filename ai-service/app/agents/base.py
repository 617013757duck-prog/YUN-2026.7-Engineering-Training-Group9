"""
Agent 基类与通用框架
"""
from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
from datetime import datetime
import uuid
from loguru import logger


class AgentRun:
    """单次 Agent 执行记录"""

    def __init__(self, agent_name: str, agent_version: str):
        self.run_id = str(uuid.uuid4())[:8]
        self.agent_name = agent_name
        self.agent_version = agent_version
        self.input: Dict = {}
        self.output: Dict = {}
        self.start_time: Optional[datetime] = None
        self.end_time: Optional[datetime] = None
        self.intermediate_steps: list = []
        self.error: Optional[str] = None

    def to_dict(self) -> Dict:
        return {
            "run_id": self.run_id,
            "agent_name": self.agent_name,
            "agent_version": self.agent_version,
            "input": self.input,
            "output": self.output,
            "start_time": self.start_time.isoformat() if self.start_time else None,
            "end_time": self.end_time.isoformat() if self.end_time else None,
            "duration_ms": (
                (self.end_time - self.start_time).total_seconds() * 1000
                if self.start_time and self.end_time else None
            ),
            "intermediate_steps": self.intermediate_steps,
            "error": self.error,
        }


class BaseAgent(ABC):
    """Agent 基类"""

    def __init__(self, name: str, version: str, llm_service, prompt_manager):
        self.name = name
        self.version = version
        self.llm_service = llm_service
        self.prompt_manager = prompt_manager

    def execute(self, **kwargs) -> AgentRun:
        """执行 Agent，返回完整的执行记录"""
        run = AgentRun(self.name, self.version)
        run.input = kwargs
        run.start_time = datetime.now()

        logger.info(f"[{self.name}] 开始执行 (run_id={run.run_id})")

        try:
            result = self._process(**kwargs)
            run.output = result
            logger.info(f"[{self.name}] 执行完成 (run_id={run.run_id})")
        except Exception as e:
            run.error = str(e)
            run.output = {"error": str(e)}
            logger.error(f"[{self.name}] 执行失败 (run_id={run.run_id}): {e}")
        finally:
            run.end_time = datetime.now()

        return run

    @abstractmethod
    def _process(self, **kwargs) -> Dict[str, Any]:
        """子类实现核心逻辑"""
        pass
