# AI 服务接入指南（成员 A/B 用）

## 一、服务概览

| 项目 | 说明 |
|------|------|
| 框架 | FastAPI (Python 3.10+) |
| 默认端口 | `8000` |
| Swagger 文档 | `http://localhost:8000/docs` |
| LLM 引擎 | DeepSeek v4 Flash API |
| 向量模型 | BAAI/bge-large-zh-v1.5 (1024维) |
| 向量数据库 | FAISS (本地文件索引) |
| 数据库 | MySQL (与后端共享 `medical_platform`) |

## 二、目录结构

```
ai-service/
├── main.py                  # 启动入口，路由注册，startup 初始化
├── .env.example             # 环境变量模板（复制为 .env 后填入真实值）
├── .gitignore
├── requirements.txt
├── data/
│   └── medical_guidelines.json   # 10篇公开医疗指南
├── app/
│   ├── config.py            # 配置类（读取 .env）
│   ├── api/                 # REST API 路由层
│   │   ├── health.py        # GET  /health          健康检查
│   │   ├── analyze.py       # POST /analyze         预留
│   │   ├── retrieve.py      # POST /retrieve        RAG检索
│   │   ├── llm.py           # POST /llm/generate    LLM推理
│   │   ├── agents.py        # POST /analyze         多Agent危险复核 ★核心
│   │   ├── safety.py        # POST /safety/check    安全检查
│   │   ├── followup.py      # POST /followup/plan   随访计划
│   │   └── version.py       # GET  /version/...     版本/审计/追溯
│   ├── agents/              # 多Agent系统
│   │   ├── base.py          # Agent基类
│   │   ├── orchestrator.py  # 编排器（串联三阶段+审计日志）
│   │   ├── symptom_agent.py # Agent1: 症状分析
│   │   ├── risk_agent.py    # Agent2: 风险判断
│   │   └── suggestion_agent.py # Agent3: 建议生成
│   ├── core/                # 核心组件
│   │   ├── database.py      # MySQL 连接
│   │   ├── llm_service.py   # DeepSeek API 调用
│   │   ├── prompt_manager.py # Prompt 模板
│   │   └── logging_config.py
│   ├── knowledge_base/      # 医疗知识库
│   │   ├── collector.py     # 指南收集
│   │   ├── cleaner.py       # 数据清洗
│   │   ├── splitter.py      # 文档切分
│   │   ├── embedder.py      # BGE 向量化
│   │   ├── vector_store.py  # FAISS 索引
│   │   ├── retriever.py     # RAG 检索
│   │   └── pipeline.py      # 知识库构建流水线
│   ├── models/
│   │   └── db_models.py     # ORM 模型（7张表）
│   ├── security/            # AI安全护栏
│   │   ├── input_guard.py   # 输入过滤（注入检测）
│   │   ├── output_guard.py  # 输出审查（越权拦截）
│   │   ├── privacy_guard.py # 隐私脱敏
│   │   └── gateway.py       # 安全网关门面
│   ├── services/
│   │   ├── followup_service.py  # 随访计划生成
│   │   ├── version_service.py   # 版本管理
│   │   ├── trace_service.py     # 调用链存储
│   │   ├── audit_service.py     # 审计日志
│   │   └── alert_service.py     # 安全告警
│   └── utils/
│       └── helpers.py       # 工具函数
```

## 三、全部 API 清单

### 3.1 核心业务接口（成员B/Spring Boot 调用）

| 方法 | 路径 | 用途 | 请求体 |
|------|------|------|--------|
| **POST** | `/api/ai/analyze` | ★ 多Agent危险复核（核心） | `{"user_symptoms":"...","visit_id":1001,"user_id":1}` |
| POST | `/api/ai/followup/plan` | 生成随访计划 | `{"visit_id":1001,"risk_level":"中","symptom_summary":"..."}` |
| POST | `/api/ai/followup/reminder` | 生成随访提醒 | `{"visit_id":1001,"risk_level":"高","next_date":"2026-08-01"}` |
| POST | `/api/ai/retrieve` | RAG知识检索 | `{"query":"头痛怎么办","top_k":5}` |
| POST | `/api/ai/llm/generate` | LLM原始推理 | `{"prompt":"...","scenario":"symptom_analysis","max_tokens":512}` |

### 3.2 安全接口

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | `/api/ai/safety/check` | 全面安全审查（输入+输出） |
| POST | `/api/ai/safety/desensitize` | 独立隐私脱敏 |

### 3.3 管理与审计接口

| 方法 | 路径 | 用途 |
|------|------|------|
| GET | `/api/ai/health` | 健康检查（含 FAISS 状态） |
| GET | `/api/ai/llm/templates` | Prompt 模板列表 |
| GET | `/api/ai/version/model` | 当前活跃模型版本 |
| GET | `/api/ai/version/model/history` | 模型版本历史 |
| GET | `/api/ai/version/knowledge-base` | 知识库版本快照 |
| GET | `/api/ai/trace/{visit_id}` | 某次就诊完整调用链 |
| GET | `/api/ai/audit/recent?limit=20` | 最近审计日志 |
| GET | `/api/ai/alerts` | 安全告警 |

### 3.4 核心接口详解

#### POST /api/ai/analyze — 对 Spring Boot 最重要的接口

**请求：**
```json
{
    "user_symptoms": "头痛3天，偶尔伴有恶心，体温37.8°C",
    "visit_id": 1001,
    "user_id": 1
}
```

**响应：**
```json
{
    "success": true,
    "visit_id": 1001,
    "agent_count": 3,
    "agent_runs": [...],
    "final_result": {
        "symptoms": ["头痛", "恶心", "低热"],
        "red_flags": [],
        "has_red_flag": false,
        "risk_level": "低",
        "is_emergency": false,
        "suggestions": "根据您的症状描述，初步判断为...",
        "citations": [
            {"source": "头痛诊疗指南", "section": "诊断标准", "score": 0.92}
        ],
        "violations_detected": []
    },
    "safety_report": {
        "blocked": false,
        "input_risk": "safe",
        "output_risk": "safe",
        "has_privacy_issue": false,
        "output_violations": []
    }
}
```

**流程图：**
```
用户症状 → 安全过滤（注入检测） → 隐私脱敏（手机号/身份证） 
→ Agent1 症状分析 → Agent2 RAG检索+风险判断 → Agent3 建议生成 
→ 安全审查（越权拦截） → 审计日志 → 调用链存储 → 返回结果
```

## 四、数据库表（AI专属）

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `medical_guidelines` | 医疗指南原文 | title, source, version, raw_content |
| `citations` | 引用记录 | visit_id, chunk_id, source, section, score |
| `model_versions` | 模型版本 | model_name, version, prompt_version, is_active |
| `knowledge_base_snapshots` | 知识库快照 | version, total_chunks, embedding_model, faiss_index_hash |
| `agent_runs` | 调用链记录 | visit_id, run_id, user_input_raw → agent_1/2/3_output → ai_output_sanitized |
| `audit_logs` | 审计日志 | event_type, user_id, visit_id, details（与后端共享） |

## 五、部署与启动

### 5.1 环境准备

```powershell
# 1. 安装依赖
cd ai-service
pip install -r requirements.txt

# 2. 配置环境变量
copy .env.example .env
# 编辑 .env，填入 DeepSeek API Key 和 MySQL 密码

# 3. 确保 MySQL 数据库存在
# 在 MySQL 中执行:
#   CREATE DATABASE IF NOT EXISTS medical_platform 
#     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 4. 构建知识库索引（仅首次，约 1-3 分钟）
python -c "from app.knowledge_base.pipeline import KnowledgeBasePipeline; p = KnowledgeBasePipeline(); p.run('data/medical_guidelines.json')"
```

### 5.2 启动服务

```powershell
python main.py
# 访问 http://localhost:8000/docs 查看 Swagger
```

### 5.3 Spring Boot 配置

```yaml
# application.yml
ai-service:
  base-url: http://localhost:8000/api/ai
  
# RestTemplate 或 Feign 调用示例:
# POST {base-url}/analyze
```

## 六、安全机制说明

| 层级 | 模块 | 拦截内容 |
|------|------|---------|
| 输入 | `input_guard` | 角色扮演绕过、越狱尝试、代码注入 |
| 隐私 | `privacy_guard` | 身份证号、手机号、银行卡、邮箱 → `***` |
| 输出 | `output_guard` | "确诊为XX"、"开药XX"、"建议服用XX" |
| 审计 | `audit_service` | 所有决策事件写入 `audit_logs` |
| 告警 | `alert_service` | 5分钟超5次注入 → 高频告警 |

**重要**：安全护栏为防御性措施。最终由真实医生审核，AI 仅提供辅助参考。

## 七、常见问题

**Q: `python main.py` 启动后报 FAISS 索引不存在？**
A: 运行一次知识库构建流水线即可（见 5.1 步骤4）。

**Q: MYSQL 连接失败？**
A: 检查 `.env` 中的 `MYSQL_HOST/PORT/USER/PASSWORD/DATABASE` 是否正确，确保 MySQL 服务已启动。

**Q: DeepSeek API 调用超时？**
A: 检查 `.env` 中的 `DEEPSEEK_API_KEY` 是否正确，API 地址是否可达。

**Q: BGE 模型下载失败？**
A: 检查 `HF_ENDPOINT` 镜像配置，或在 `.env` 中切换为其他国内镜像。
