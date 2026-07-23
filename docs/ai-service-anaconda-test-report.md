# AI服务Anaconda环境测试报告

**测试时间：** 2026-07-23  
**测试人员：** 贺孟缘（后端开发）  
**测试环境：** Anaconda Python 3.10.20 + Windows 10

---

## 一、环境配置过程

### 1.1 环境创建 ✅
- **Conda版本**：Conda 4.5.11
- **Python版本**：Python 3.10.20（从Anaconda中选择）
- **环境名称**：medical-ai
- **环境路径**：D:\Anaconda3\envs\medical-ai

**创建命令：**
```bash
D:\Anaconda3\Scripts\conda.exe create -n medical-ai python=3.10 -y
```

**安装包（20个）：**
- python-3.10.20
- pip-26.1.2
- setuptools-83.0.0
- wheel-0.47.0
- openssl-3.5.7
- sqlite-3.53.2
- 等等...

---

### 1.2 依赖包安装 ✅

**安装命令：**
```bash
D:\Anaconda3\envs\medical-ai\python.exe -m pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

**成功安装的包（83个）：**

**核心框架：**
- fastapi-0.139.2
- uvicorn-0.51.0
- pydantic-2.13.4
- starlette-1.3.1

**深度学习：**
- torch-2.13.0 (122 MB)
- transformers-5.14.1 (11.6 MB)
- sentence-transformers-5.6.0
- scikit-learn-1.7.2
- scipy-1.15.3 (41.3 MB)
- numpy-2.2.6 (12.9 MB)

**向量检索：**
- faiss-cpu-1.14.3 (16.2 MB)

**API和工具：**
- openai-2.47.0
- huggingface-hub-1.24.0
- langchain-1.3.14
- langgraph-1.2.9
- sqlalchemy-2.0.51
- pymysql-1.2.0
- loguru-0.7.3
- python-dotenv-1.2.2

**总下载量：** 约200+ MB
**安装时间：** 约15分钟

---

## 二、AI服务启动测试

### 2.1 启动命令
```bash
D:\Anaconda3\envs\medical-ai\python.exe main.py
```

### 2.2 启动失败 ❌

**错误信息：**
```
OSError: We couldn't connect to 'https://hf-mirror.com' to load the files, and couldn't find them in the cached files.
Check your internet connection or see how to run the library in offline mode at 'https://huggingface.co/docs/transformers/installation#offline-mode'.
```

**根本原因：**
- AI服务需要下载预训练的BGE嵌入模型
- 无法连接到Hugging Face镜像站点（https://hf-mirror.com）
- 模型未缓存到本地

**影响范围：**
- 无法启动AI服务
- 无法进行RAG检索
- 无法进行AI分析

---

## 三、问题分析

### 3.1 技术分析
AI服务在启动时会立即初始化嵌入模型：
```python
# app/knowledge_base/retriever.py
class MedicalRetriever:
    def __init__(self):
        self.embedder = BGEEmbedder()  # 这里会触发模型下载

# app/knowledge_base/embedder.py  
class BGEEmbedder:
    def __init__(self):
        self.model = SentenceTransformer(
            model_name_or_path="BAAI/bge-small-zh-v1.5"
        )
```

### 3.2 网络问题
- **Hugging Face镜像**：https://hf-mirror.com（中国镜像）
- **模型大小**：BGE-small-zh-v1.5约100-200MB
- **网络状态**：无法连接或超时

---

## 四、解决方案

### 4.1 方案一：配置网络代理（推荐）

**步骤：**
1. 配置代理或VPN
2. 设置环境变量：
   ```bash
   set HTTP_PROXY=http://127.0.0.1:7890
   set HTTPS_PROXY=http://127.0.0.1:7890
   ```
3. 重新启动AI服务：
   ```bash
   D:\Anaconda3\envs\medical-ai\python.exe main.py
   ```

**优点：**
- 一次性解决，模型会缓存到本地
- 后续启动无需再次下载

### 4.2 方案二：手动下载模型

**步骤：**
1. 使用代理或VPN从以下地址下载模型：
   - https://huggingface.co/BAAI/bge-small-zh-v1.5
2. 将模型保存到本地目录
3. 修改embedder.py代码：
   ```python
   self.model = SentenceTransformer(
       model_name_or_path="本地路径/bge-small-zh-v1.5"
   )
   ```

**优点：**
- 不依赖实时网络连接
- 可以离线运行

### 4.3 方案三：延迟加载模型

**修改代码：**
```python
# app/knowledge_base/embedder.py
class BGEEmbedder:
    def __init__(self):
        self.model = None
    
    def get_model(self):
        if self.model is None:
            self.model = SentenceTransformer(
                model_name_or_path="BAAI/bge-small-zh-v1.5"
            )
        return self.model
```

**优点：**
- 服务可以快速启动
- 只在实际使用时下载模型

### 4.4 方案四：使用Mock服务测试后端

**创建Mock AI服务：**
```python
# mock_ai_service.py
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

@app.get("/api/ai/health")
async def health():
    return {"status": "ok"}

@app.post("/api/ai/analyze")
async def analyze(request: dict):
    return {
        "visit_id": request["visit_id"],
        "risk_level": "中风险",
        "analysis": "这是Mock测试响应",
        "suggestions": ["建议1", "建议2"],
        "model_version": "mock-v1.0",
        "timestamp": "2026-07-23T15:00:00"
    }
```

**启动Mock服务：**
```bash
D:\Anaconda3\envs\medical-ai\python.exe -m uvicorn mock_ai_service:app --host 0.0.0.0 --port 8000
```

**优点：**
- 快速启动，无需下载模型
- 可以测试后端集成逻辑

---

## 五、测试结果汇总

| 测试项 | 状态 | 说明 |
|--------|------|------|
| Conda环境创建 | ✅ 通过 | Python 3.10.20环境创建成功 |
| 依赖包安装 | ✅ 通过 | 83个包全部安装成功 |
| 模型下载 | ❌ 失败 | 网络连接问题 |
| AI服务启动 | ❌ 失败 | 缺少预训练模型 |
| 健康检查接口 | ⏸️ 待测 | 服务未启动 |
| RAG检索接口 | ⏸️ 待测 | 服务未启动 |
| AI分析接口 | ⏸️ 待测 | 服务未启动 |

---

## 六、环境信息记录

### 6.1 Conda环境信息
```
环境名称: medical-ai
Python版本: 3.10.20
环境路径: D:\Anaconda3\envs\medical-ai
包数量: 83个
```

### 6.2 Python执行路径
```
D:\Anaconda3\envs\medical-ai\python.exe
```

### 6.3 已安装的关键包
```
torch==2.13.0
transformers==5.14.1
sentence-transformers==5.6.0
fastapi==0.139.2
uvicorn==0.51.0
faiss-cpu==1.14.3
openai==2.47.0
langchain==1.3.14
```

---

## 七、后续建议

### 7.1 立即行动
1. **配置网络代理**：使用代理下载模型
2. **或创建Mock服务**：先测试后端集成
3. **或联系AI组员**：询问模型下载方案

### 7.2 长期方案
1. **离线部署**：将模型打包到项目中
2. **模型预热**：CI/CD流程中预先下载模型
3. **文档完善**：添加模型下载和配置说明

---

## 八、总结

### 8.1 环境配置成功 ✅
- Conda环境创建成功
- 83个依赖包安装成功
- Python 3.10版本兼容性良好

### 8.2 网络问题阻碍 ❌
- 无法连接Hugging Face镜像
- 预训练模型未下载
- AI服务无法启动

### 8.3 解决方案多样
- 配置代理（推荐）
- 手动下载模型
- 延迟加载
- Mock服务测试

### 8.4 后端就绪 ✅
- 后端代码编译成功
- 接口格式设计正确
- 等待AI服务就绪

---

**结论：** Anaconda环境配置成功，依赖包安装完整。主要障碍是网络问题导致无法下载预训练模型。建议配置网络代理或使用Mock服务先行测试后端集成。

**下一步：** 
1. 配置网络代理下载模型
2. 或创建Mock服务测试集成
3. 完成端到端测试