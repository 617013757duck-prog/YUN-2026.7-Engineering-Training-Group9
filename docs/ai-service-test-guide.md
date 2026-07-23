# AI服务测试指南

**测试时间：** 2026-07-23  
**测试目的：** 验证AI服务与后端的集成，确认API接口功能正常

---

## 一、环境配置状态

### 1.1 已完成配置
- ✅ DeepSeek API密钥已配置（sk-65f78bafe85246f0a7722f9642fb9fd9）
- ✅ .env文件已创建
- ✅ 数据库配置已设置（密码：07040528hmy）

### 1.2 待配置环境
- ❌ Python环境未安装（需要Python 3.8+）
- ⏳ Python依赖包需要安装
- ⏳ AI服务需要启动

---

## 二、环境安装指南

### 2.1 安装Python

**方案1：从官网下载安装**
1. 访问：https://www.python.org/downloads/
2. 下载Python 3.10或3.11版本
3. 安装时勾选"Add Python to PATH"
4. 验证安装：`python --version`

**方案2：使用Anaconda（推荐）**
1. 访问：https://www.anaconda.com/download
2. 下载并安装Anaconda
3. 创建虚拟环境：
   ```bash
   conda create -n medical-ai python=3.10
   conda activate medical-ai
   ```

### 2.2 安装依赖包

```bash
# 进入AI服务目录
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\ai-service

# 安装依赖
pip install -r requirements.txt

# 或使用国内镜像加速
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 2.3 启动AI服务

```bash
# 方式1：直接运行
python main.py

# 方式2：使用uvicorn
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

---

## 三、API接口测试方案

### 3.1 使用curl测试

**测试1：健康检查**
```bash
curl -X GET http://localhost:8000/api/ai/health
```

**预期响应：**
```json
{
  "status": "ok",
  "message": "AI服务运行正常",
  "timestamp": "2026-07-23T15:00:00"
}
```

**测试2：RAG检索**
```bash
curl -X POST http://localhost:8000/api/ai/retrieve \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"胸痛\",\"visit_id\":1,\"top_k\":5}"
```

**预期响应：**
```json
{
  "success": true,
  "query": "胸痛",
  "results": [...],
  "context": "...",
  "total": 5
}
```

**测试3：AI分析**
```bash
curl -X POST http://localhost:8000/api/ai/analyze \
  -H "Content-Type: application/json" \
  -d "{\"visit_id\":1,\"symptoms\":[\"胸痛\"],\"patient_age\":45,\"duration_days\":3}"
```

**预期响应：**
```json
{
  "visit_id": 1,
  "risk_level": "中风险",
  "analysis": "...",
  "suggestions": [...],
  "model_version": "deepseek-v4-flash",
  "timestamp": "2026-07-23T15:00:00"
}
```

### 3.2 使用Postman测试

**导入环境变量：**
- `BASE_URL`: `http://localhost:8000/api/ai`

**创建Collection：**
1. Health Check - GET `{{BASE_URL}}/health`
2. RAG Retrieve - POST `{{BASE_URL}}/retrieve`
3. AI Analyze - POST `{{BASE_URL}}/analyze`

### 3.3 使用Python脚本测试

**创建测试脚本：** `test_ai_api.py`

```python
import requests
import json

BASE_URL = "http://localhost:8000/api/ai"

def test_health():
    """测试健康检查"""
    response = requests.get(f"{BASE_URL}/health")
    print(f"健康检查: {response.status_code}")
    print(response.json())
    return response.status_code == 200

def test_retrieve():
    """测试RAG检索"""
    payload = {
        "query": "胸痛伴呼吸困难",
        "visit_id": 1,
        "top_k": 5
    }
    response = requests.post(f"{BASE_URL}/retrieve", json=payload)
    print(f"RAG检索: {response.status_code}")
    data = response.json()
    print(f"成功: {data['success']}, 结果数: {data['total']}")
    return response.status_code == 200

def test_analyze():
    """测试AI分析"""
    payload = {
        "visit_id": 1,
        "symptoms": ["胸痛", "呼吸困难"],
        "patient_age": 45,
        "duration_days": 3,
        "additional_info": "主诉胸痛3天"
    }
    response = requests.post(f"{BASE_URL}/analyze", json=payload)
    print(f"AI分析: {response.status_code}")
    data = response.json()
    print(f"风险等级: {data['risk_level']}")
    print(f"分析结果: {data['analysis'][:100]}...")
    return response.status_code == 200

if __name__ == "__main__":
    print("=" * 50)
    print("开始测试AI服务API")
    print("=" * 50)
    
    # 执行测试
    results = []
    results.append(("健康检查", test_health()))
    results.append(("RAG检索", test_retrieve()))
    results.append(("AI分析", test_analyze()))
    
    # 打印结果
    print("\n" + "=" * 50)
    print("测试结果汇总")
    print("=" * 50)
    for name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{name}: {status}")
    
    # 统计
    passed = sum(1 for _, r in results if r)
    total = len(results)
    print(f"\n总计: {passed}/{total} 测试通过")
    
    if passed == total:
        print("\n🎉 所有测试通过！AI服务运行正常")
    else:
        print(f"\n⚠️  有{total - passed}个测试失败，请检查日志")
```

**运行测试：**
```bash
# 安装requests库
pip install requests

# 运行测试
python test_ai_api.py
```

---

## 四、后端集成测试

### 4.1 启动后端服务

```bash
# 进入后端目录
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\backend

# 启动Spring Boot
mvn spring-boot:run
```

### 4.2 测试完整流程

**步骤1：获取JWT Token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

**步骤2：创建就诊记录**
```bash
curl -X POST http://localhost:8080/api/visits \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"patientName\":\"测试患者\",\"patientPhone\":\"13800138000\",\"age\":45,\"gender\":\"男\",\"chiefComplaint\":\"胸痛3天\",\"symptoms\":[{\"symptomName\":\"胸痛\",\"severity\":\"中度\",\"duration\":\"3天\"}]}"
```

**步骤3：执行AI分析**
```bash
curl -X POST http://localhost:8080/api/visits/1/analyze \
  -H "Authorization: Bearer <token>"
```

**步骤4：检查结果**
```bash
curl -X GET http://localhost:8080/api/visits/1 \
  -H "Authorization: Bearer <token>"
```

---

## 五、故障排查

### 5.1 常见问题

**问题1：Python未安装**
- 解决：安装Python 3.8+或Anaconda

**问题2：依赖包安装失败**
- 解决：使用国内镜像
  ```bash
  pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
  ```

**问题3：DeepSeek API调用失败**
- 检查：API密钥是否正确
- 检查：网络连接是否正常
- 检查：API额度是否充足

**问题4：数据库连接失败**
- 检查：MySQL服务是否启动
- 检查：数据库密码是否正确
- 检查：数据库是否已创建

**问题5：向量库加载失败**
- 原因：首次启动索引不存在
- 解决：正常现象，需要先构建向量库

### 5.2 日志查看

**AI服务日志：**
- 位置：控制台输出
- 查看方式：观察启动日志和错误信息

**后端日志：**
- 位置：`backend/logs/`目录
- 配置：`application.yml`中的logging配置

---

## 六、测试报告模板

### 6.1 测试结果记录表

| 测试项 | 测试内容 | 预期结果 | 实际结果 | 状态 | 备注 |
|-------|---------|---------|---------|------|------|
| 环境配置 | Python环境 | 成功安装 | - | ⏳ | 待安装 |
| 环境配置 | 依赖包安装 | 全部安装 | - | ⏳ | 待安装 |
| 接口测试 | 健康检查 | 返回200 | - | ⏳ | 待测试 |
| 接口测试 | RAG检索 | 返回结果 | - | ⏳ | 待测试 |
| 接口测试 | AI分析 | 风险评级 | - | ⏳ | 待测试 |
| 集成测试 | 后端调用AI | 成功集成 | - | ⏳ | 待测试 |

### 6.2 问题记录表

| 序号 | 发现时间 | 问题描述 | 严重程度 | 解决方案 | 状态 |
|-----|---------|---------|---------|---------|------|
| 1 | 2026-07-23 | Python未安装 | 高 | 安装Python | 待解决 |
| 2 | - | - | - | - | - |

---

## 七、后续行动

### 7.1 立即行动
1. **安装Python环境**（必须）
2. **安装依赖包**（必须）
3. **启动AI服务**（必须）
4. **执行API测试**（必须）

### 7.2 建议行动
1. 与AI组员沟通，确认AI服务运行状态
2. 获取AI服务的完整文档和测试数据
3. 编写自动化测试脚本
4. 记录测试结果和发现的问题

### 7.3 可选行动
1. 使用Docker容器化部署（推荐）
2. 搭建CI/CD自动化测试流程
3. 配置日志收集和监控系统

---

## 八、联系信息

**如遇到问题，请联系：**
- **后端开发**：贺孟缘
- **AI开发**：请咨询负责AI部分的成员
- **项目经理**：协调资源和解决问题

---

**备注：** 
1. DeepSeek API密钥已配置（sk-65f78bafe85246f0a7722f9642fb9fd9）
2. 由于Python环境未安装，暂时无法启动AI服务进行测试
3. 建议尽快安装Python环境，或联系AI组员协助测试
4. 测试完成后请填写测试报告，记录测试结果和发现的问题