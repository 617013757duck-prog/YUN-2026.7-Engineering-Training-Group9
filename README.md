# 基层医疗安全型预问诊与随访平台

## 项目简介

这是一个面向基层医疗的教学辅助系统，提供预问诊和随访管理功能。系统不提供真实诊断，仅用于教学演示和模拟场景。

## 项目成员与分工

### 成员A - 前端开发负责人
- **职责**: 用户界面与交互体验
- **技术栈**: Vue 3 + Element Plus + Vite
- **工作目录**: `frontend/`
- **主要任务**:
  - 分步骤预问诊表单
  - 医务人员工作台和待处理队列
  - 症状时间线、风险等级可视化展示
  - 指南检索界面及原文对照展示
  - 随访计划管理界面
  - 高风险案例和AI安全监控面板

### 成员B - 后端核心开发负责人
- **职责**: 业务逻辑、安全机制、API设计
- **技术栈**: Spring Boot 3 + MySQL + Redis + MyBatis Plus
- **工作目录**: `backend/`
- **主要任务**:
  - JWT 登录认证和 RBAC 权限控制
  - 敏感字段脱敏机制
  - 就诊和随访业务状态机实现
  - 结构化规则引擎（红旗症状、禁忌条件检查）
  - RESTful API 设计与接口文档
  - 数据库核心表设计与实现

### 成员C - AI系统与数据智能负责人
- **职责**: AI功能、知识库、安全监控
- **技术栈**: Python + FastAPI + ChatGLM3 + LangChain
- **工作目录**: `ai-service/`
- **主要任务**:
  - 医疗 RAG 系统构建（指南检索与知识库）
  - 多 Agent 风险复核系统
  - AI 输出安全 Agent
  - 模型版本管理
  - 随访计划和提醒逻辑实现
  - 审计日志和安全告警系统

## 技术栈

### 前端
- Vue 3.4+
- Element Plus 2.6+
- Vue Router 4
- Pinia
- Axios
- ECharts

### 后端
- Spring Boot 3.2+
- Spring Security + JWT
- MyBatis Plus 3.5+
- MySQL 8.0+
- Redis

### AI 服务
- Python 3.10+
- FastAPI
- ChatGLM3-6B / Qwen-7B
- LangChain
- FAISS 向量数据库

## 项目结构

```
.
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/medical/platform/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── entity/
│   │   │   │   ├── dto/
│   │   │   │   ├── config/
│   │   │   │   ├── security/
│   │   │   │   ├── exception/
│   │   │   │   └── utils/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
│
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── views/
│   │   ├── router/
│   │   ├── store/
│   │   ├── api/
│   │   └── utils/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── ai-service/                 # Python AI 服务
│   ├── app/
│   │   ├── core/
│   │   ├── agents/
│   │   ├── api/
│   │   └── utils/
│   ├── data/
│   ├── models/
│   ├── knowledge_base/
│   ├── main.py
│   └── requirements.txt
│
├── database/                   # 数据库脚本
│   ├── init.sql
│   └── test_data.sql
│
├── docs/                       # 项目文档
│   ├── API文档.md
│   ├── 部署指南.md
│   └── 开发规范.md
│
└── README.md
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Python 3.10+
- MySQL 8.0+
- Redis 6.0+

### 1. 数据库初始化
```bash
mysql -u root -p < database/init.sql
```

### 2. 后端启动
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

访问 API 文档: http://localhost:8080/doc.html

### 3. 前端启动
```bash
cd frontend
npm install
npm run dev
```

访问前端: http://localhost:3000

### 4. AI 服务启动
```bash
cd ai-service
pip install -r requirements.txt
python main.py
```

访问 AI 服务 API 文档: http://localhost:8000/docs

## 默认账户

- **用户名**: admin
- **密码**: admin123

## 重要说明

1. **数据合规**: 必须使用合成病例和公开指南，不得保存真实医疗数据
2. **安全规范**: 所有敏感字段必须脱敏，完整审计日志
3. **版本管理**: 使用 GitHub Projects 进行任务跟踪
4. **技术栈统一**: 请严格按照指定的技术栈开发

## 开发里程碑

### 第1周: 基础架构
- A: 搭建 Vue 项目框架，完成登录页面
- B: 搭建 Spring Boot 项目，完成数据库设计和 JWT 认证
- C: 部署本地模型，测试推理接口，收集医疗指南数据

### 第2-3周: 核心功能
- A: 完成预问诊表单、医务人员工作台
- B: 实现就诊状态机、规则引擎、随访模块
- C: 构建 RAG 系统，实现基础 Agent 功能

### 第4周: 集成与安全
- A: 对接后端 API，实现 AI 监控面板
- B: 集成 Python AI 服务，实现安全拦截逻辑
- C: 完善 AI 安全护栏，实现版本管理

### 第5周: 测试与优化
- 全员: 联调测试、Bug 修复、性能优化

## 项目链接

- GitHub 仓库: https://github.com/617013757duck-prog/YUN-2026.7-Engineering-Training-Group9
- GitHub Projects: https://github.com/users/617013757duck-prog/projects/2/views/1

## 许可证

本项目仅供教学使用，不得用于实际医疗场景。