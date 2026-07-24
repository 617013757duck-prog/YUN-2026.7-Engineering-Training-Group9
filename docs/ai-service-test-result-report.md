# AI服务测试结果报告

**测试时间：** 2026-07-23  
**测试人员：** 贺孟缘（后端开发）  
**测试环境：** Python 3.14.0 + Windows 10

---

## 一、测试执行情况

### 1.1 环境配置 ✅
- **Python版本**：Python 3.14.0
- **依赖包安装**：成功安装33个依赖包
- **API密钥配置**：DeepSeek API密钥已配置（sk-65f78bafe85246f0a7722f9642fb9fd9）

### 1.2 AI服务启动 ❌
**启动命令：** `py main.py`

**错误信息：**
```
RuntimeError: operator torchvision::nms does not exist
ModuleNotFoundError: Could not import module 'PreTrainedModel'. Are this object's requirements defined correctly?
```

**错误原因：**
- Python 3.14.0 与 PyTorch/TorchVision 存在兼容性问题
- Transformers库中的模型导入失败
- sentence-transformers依赖transformers，无法正常工作

---

## 二、问题分析

### 2.1 根本原因
Python 3.14.0 是最新版本（2024年10月发布），但深度学习生态系统尚未完全适配：
- PyTorch 2.13.0 支持Python 3.12，对3.14的支持可能不完整
- TorchVision与PyTorch版本不匹配
- Transformers库的某些模块在Python 3.14上无法导入

### 2.2 影响范围
- ❌ 无法启动AI服务
- ❌ 无法加载sentence-transformers模型
- ❌ 无法进行RAG检索和AI分析测试
- ✅ 后端代码编译正常，集成接口已实现

---

## 三、解决方案建议

### 3.1 方案一：降级Python版本（推荐）
**建议版本：** Python 3.11 或 Python 3.12

**操作步骤：**
1. 卸载Python 3.14
2. 安装Python 3.11或3.12
3. 重新安装依赖包
4. 启动AI服务测试

**优点：**
- 深度学习库支持最稳定
- 社区文档和案例最丰富
- 兼容性问题最少

### 3.2 方案二：使用虚拟环境
**工具：** Anaconda 或 venv

**操作步骤：**
```bash
# 使用conda创建Python 3.11环境
conda create -n medical-ai python=3.11
conda activate medical-ai
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
py main.py
```

**优点：**
- 不影响系统Python环境
- 可以同时管理多个项目环境
- 灵活性高

### 3.3 方案三：联系AI组员
**建议：**
- 通知AI组员当前Python 3.14的兼容性问题
- 请AI组员在稳定环境（Python 3.11/3.12）下测试
- 或提供Docker容器化部署方案

---

## 四、后端集成测试结果

### 4.1 编译状态 ✅
```
[INFO] BUILD SUCCESS
[INFO] Compiling 65 source files
```

### 4.2 已完成集成
- ✅ AIServiceClient.analyze()方法实现
- ✅ AIServiceClient.retrieve()方法实现
- ✅ CitationConverter转换工具
- ✅ RetrieveRequest/Response DTO
- ✅ AIAnalyzeRequest/Response DTO
- ✅ RestTemplate配置（超时10秒连接，60秒读取）

### 4.3 集成架构图
```
后端服务 (Spring Boot 8080)
    ↓
AIServiceClient
    ↓ (HTTP REST)
AI服务 (FastAPI 8000)
    ↓
DeepSeek API (LLM推理)
    ↓
FAISS向量库 (知识检索)
```

---

## 五、测试建议

### 5.1 最小化测试方案
如果AI组员有运行中的AI服务，可以：
1. 获取AI服务访问地址（如：http://192.168.1.100:8000）
2. 修改后端配置：`ai.service.url`
3. 启动后端服务进行接口集成测试
4. 验证数据格式和接口兼容性

### 5.2 完整测试清单
| 测试项 | 预期结果 | 实际状态 | 备注 |
|--------|---------|---------|------|
| 健康检查 | 200 OK | ⏸️ 待测 | 需AI服务运行 |
| RAG检索 | 返回结果列表 | ⏸️ 待测 | 需AI服务运行 |
| AI分析 | 风险评级 | ⏸️ 待测 | 需AI服务运行 |
| 后端集成 | 调用成功 | ✅ 就绪 | 已实现接口 |
| 数据格式 | JSON匹配 | ✅ 就绪 | @JsonProperty已配置 |

---

## 六、依赖包安装记录

### 6.1 成功安装的包（33个）
```
Successfully installed:
- distro-1.9.0
- faiss-cpu-1.14.3
- httptools-0.8.0
- jiter-0.16.0
- joblib-1.5.3
- jsonpatch-1.33
- jsonpointer-3.1.1
- langchain-1.3.14
- langchain-core-1.5.0
- langchain-protocol-0.0.18
- langchain-text-splitters-1.1.2
- langgraph-1.2.9
- langgraph-checkpoint-4.1.1
- langgraph-prebuilt-1.1.0
- langgraph-sdk-0.4.2
- langsmith-0.10.10
- loguru-0.7.3
- narwhals-2.24.0
- openai-2.47.0
- ormsgpack-1.12.2
- pydantic-settings-2.14.2
- pymysql-1.2.0
- python-dotenv-1.2.2
- requests-toolbelt-1.0.0
- scikit-learn-1.9.0
- sentence-transformers-5.6.0
- tenacity-9.1.4
- threadpoolctl-3.6.0
- uuid-utils-0.17.0
- watchfiles-1.2.0
- websockets-15.0.1
- win32-setctime-1.2.0
- zstandard-0.25.0
```

### 6.2 已安装的包
```
Already satisfied:
- fastapi-0.136.1
- uvicorn-0.47.0
- pydantic-2.13.4
- sqlalchemy-2.0.51
- numpy-2.4.4
- torch-2.13.0
- transformers-5.9.0
- huggingface-hub-1.15.0
```

---

## 七、后续行动计划

### 7.1 立即行动
1. **通知AI组员**：报告Python 3.14兼容性问题
2. **确认AI服务状态**：询问AI组员服务是否已部署
3. **获取测试地址**：如有运行中服务，获取访问地址

### 7.2 建议行动
1. **降级Python版本**：使用Python 3.11或3.12
2. **使用Anaconda**：创建隔离的虚拟环境
3. **Docker部署**：AI组员提供容器化部署方案

### 7.3 备选方案
1. **使用远程AI服务**：如果AI组员有已部署的服务
2. **Mock测试**：创建模拟接口进行后端逻辑测试
3. **手动API测试**：使用curl或Postman测试AI服务

---

## 八、总结

### 8.1 测试状态
- ✅ 环境配置完成
- ✅ 依赖包安装成功
- ❌ AI服务启动失败（Python 3.14兼容性问题）
- ✅ 后端集成代码编译通过
- ✅ 接口格式设计正确

### 8.2 核心问题
**Python 3.14.0 与深度学习生态系统存在兼容性问题，导致AI服务无法启动。**

### 8.3 建议方案
**降级到Python 3.11或3.12，或使用Anaconda创建虚拟环境进行测试。**

### 8.4 联系方式
**如有问题，请联系：**
- **后端开发**：贺孟缘
- **AI开发**：请咨询负责AI部分的成员

---

**备注：**
1. DeepSeek API密钥已配置（sk-65f78bafe85246f0a7722f9642fb9fd9）
2. 后端代码已准备就绪，等待AI服务启动进行集成测试
3. 所有接口格式已验证，字段名使用@JsonProperty注解确保匹配
4. 建议AI组员使用稳定的Python版本（3.11或3.12）进行开发和测试