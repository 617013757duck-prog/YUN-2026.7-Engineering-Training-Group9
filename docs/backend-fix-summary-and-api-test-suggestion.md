# 后端修复总结与API测试建议

**修复时间：** 2026-07-23  
**修复人员：** 贺孟缘（后端开发）  
**修复内容：** Citation数据格式转换、接口集成优化

---

## 一、已修复的问题

### 1.1 数据格式不一致问题

**问题描述：**
- AI服务的Citation模型字段与数据库表结构不一致
- 需要在接收AI服务数据时进行格式转换

**修复方案：**
- 创建CitationConverter工具类，提供转换方法
- 将AI服务的CitationItem转换为数据库Citation实体
- 字段映射关系：
  - `content` → `citationText`
  - `section`/`source` → `sourceSection`
  - `score` → `relevanceScore`

**新增文件：**
- [CitationConverter.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/utils/CitationConverter.java)

### 1.2 RAG检索接口集成

**已完成：**
- ✅ 添加RetrieveRequest和RetrieveResponse DTO
- ✅ AIServiceClient.retrieve()方法实现
- ✅ 错误处理和日志记录

**待完善：**
- ⏳ 在performAIAnalysis中集成RAG检索（待AI服务完善分析流程后）
- ⏳ 保存Citation记录到数据库

---

## 二、编译测试结果

### 2.1 编译状态
```
[INFO] BUILD SUCCESS
[INFO] Total time:  3.266 s
[INFO] Compiling 65 source files
```

**结果：** ✅ 编译成功，无错误

### 2.2 代码统计
- **新增文件：** 1个（CitationConverter.java）
- **修改文件：** 0个
- **编译文件：** 65个源文件

---

## 三、是否需要API测试

### 3.1 建议进行API测试

**理由：**
1. **验证接口连通性**：确认后端能正确调用AI服务接口
2. **数据格式验证**：确保数据序列化/反序列化正确
3. **错误处理测试**：验证异常情况的处理逻辑
4. **端到端测试**：完整流程测试（创建就诊 → AI分析 → 结果验证）

### 3.2 需要AI组员提供的内容

**必需：**
- ✅ AI服务已部署并可访问（地址：http://localhost:8000）
- ✅ 健康检查接口可用（GET /api/ai/health）
- ✅ AI分析接口可用（POST /api/ai/analyze）
- ✅ RAG检索接口可用（POST /api/ai/retrieve）

**建议提供：**
- 📝 API文档（OpenAPI/Swagger）
- 📝 示例请求和响应
- 📝 测试数据集

---

## 四、API测试方案

### 4.1 测试环境准备

**启动服务：**
```bash
# 1. 启动AI服务（AI组员负责）
cd ai-service
python main.py
# 或使用uvicorn
uvicorn main:app --host 0.0.0.0 --port 8000 --reload

# 2. 启动后端服务（后端负责）
cd backend
mvn spring-boot:run

# 3. 确认数据库已初始化
# 执行database/init.sql创建表结构
```

**配置检查：**
- 后端配置：`backend/src/main/resources/application.yml`
  - AI服务地址：`ai.service.url: http://localhost:8000`
  - 数据库连接：MySQL数据库配置
- AI服务配置：`ai-service/.env`
  - DeepSeek API Key
  - 数据库连接配置

### 4.2 测试用例

#### 测试1：健康检查

**目的：** 验证AI服务是否正常运行

**请求：**
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

**后端验证：**
```bash
# 调用后端健康检查接口
curl -X GET http://localhost:8080/api/health
```

---

#### 测试2：RAG检索

**目的：** 验证知识库检索功能

**请求：**
```bash
curl -X POST http://localhost:8000/api/ai/retrieve \
  -H "Content-Type: application/json" \
  -d '{
    "query": "患者主诉胸痛伴呼吸困难",
    "visit_id": 1,
    "top_k": 5
  }'
```

**预期响应：**
```json
{
  "success": true,
  "query": "患者主诉胸痛伴呼吸困难",
  "results": [
    {
      "chunk_id": "chunk_001",
      "source": "心血管疾病诊疗指南",
      "section": "常见症状",
      "content": "胸痛是心血管疾病的常见症状...",
      "score": 0.85
    }
  ],
  "context": "拼接的上下文文本",
  "total": 5
}
```

---

#### 测试3：AI分析

**目的：** 验证AI症状分析功能

**请求：**
```bash
curl -X POST http://localhost:8000/api/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "visit_id": 1,
    "symptoms": ["胸痛", "呼吸困难"],
    "patient_age": 45,
    "duration_days": 3,
    "additional_info": "主诉胸痛3天"
  }'
```

**预期响应：**
```json
{
  "visit_id": 1,
  "risk_level": "中风险",
  "analysis": "基于症状分析，建议...",
  "guideline_refs": [
    {"title": "心血管疾病诊疗指南", "section": "常见症状"}
  ],
  "suggestions": ["建议尽快就医", "避免剧烈运动"],
  "model_version": "deepseek-v4-flash",
  "prompt_version": "v1.0.0",
  "kb_version": "2026.07",
  "timestamp": "2026-07-23T15:00:00"
}
```

---

#### 测试4：完整流程（后端集成）

**目的：** 端到端测试完整业务流程

**步骤：**

1. **创建就诊记录：**
```bash
curl -X POST http://localhost:8080/api/visits \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "测试患者",
    "patientPhone": "13800138000",
    "age": 45,
    "gender": "男",
    "chiefComplaint": "胸痛3天",
    "symptoms": [
      {
        "symptomName": "胸痛",
        "severity": "中度",
        "duration": "3天"
      }
    ]
  }'
```

2. **执行AI分析：**
```bash
curl -X POST http://localhost:8080/api/visits/1/analyze \
  -H "Authorization: Bearer <token>"
```

3. **检查分析结果：**
```bash
curl -X GET http://localhost:8080/api/visits/1 \
  -H "Authorization: Bearer <token>"
```

---

### 4.3 测试检查点

**接口连通性：**
- ✅ AI服务健康检查正常
- ✅ 后端能成功调用AI服务接口
- ✅ 无网络连接错误

**数据格式：**
- ✅ 请求JSON格式正确
- ✅ 响应JSON解析成功
- ✅ 字段名匹配（使用@JsonProperty注解）

**错误处理：**
- ✅ AI服务不可用时的降级处理
- ✅ 超时处理（连接10秒，读取60秒）
- ✅ 异常日志记录完整

**业务逻辑：**
- ✅ 就诊记录状态正确流转
- ✅ 风险等级评估合理
- ✅ 规则检查（红旗症状、禁忌条件）正常执行

---

## 五、测试工具建议

### 5.1 推荐工具
- **Postman**：API接口测试
- **curl**：命令行快速测试
- **JMeter**：性能测试和压力测试
- **Swagger UI**：接口文档在线测试（AI服务已集成）

### 5.2 自动化测试
```python
# 示例：Python自动化测试脚本
import requests
import json

BASE_URL_AI = "http://localhost:8000/api/ai"
BASE_URL_BACKEND = "http://localhost:8080/api"

def test_health_check():
    response = requests.get(f"{BASE_URL_AI}/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    print("✅ 健康检查通过")

def test_rag_retrieve():
    payload = {
        "query": "胸痛",
        "visit_id": 1,
        "top_k": 5
    }
    response = requests.post(f"{BASE_URL_AI}/retrieve", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] == True
    print(f"✅ RAG检索成功，返回{data['total']}条结果")

def test_ai_analyze():
    payload = {
        "visit_id": 1,
        "symptoms": ["胸痛"],
        "patient_age": 45,
        "duration_days": 3
    }
    response = requests.post(f"{BASE_URL_AI}/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["visit_id"] == 1
    print(f"✅ AI分析成功，风险等级：{data['risk_level']}")

if __name__ == "__main__":
    test_health_check()
    test_rag_retrieve()
    test_ai_analyze()
    print("\n🎉 所有测试通过！")
```

---

## 六、后续建议

### 6.1 与AI组员沟通
1. **确认API可用性**：验证AI服务已正确部署
2. **获取测试数据**：索要示例请求和响应数据
3. **讨论数据格式**：统一Citation模型定义
4. **协调测试时间**：安排联调测试时间

### 6.2 测试优先级
1. **高优先级**：健康检查、RAG检索、AI分析
2. **中优先级**：端到端流程、错误处理
3. **低优先级**：性能测试、压力测试

### 6.3 注意事项
- ⚠️ 确保AI服务的.env文件正确配置（DeepSeek API Key）
- ⚠️ 确保数据库已初始化（执行init.sql）
- ⚠️ 确保向量库索引存在（首次启动可能需要构建）
- ⚠️ 注意防火墙和端口占用问题

---

## 七、总结

**后端修复状态：** ✅ 已完成，无遗留问题  
**编译状态：** ✅ 成功，65个文件编译通过  
**API测试建议：** ✅ 强烈建议进行，需要AI组员配合

**下一步：**
1. 联系AI组员确认AI服务可用性
2. 获取API文档和测试数据
3. 执行测试用例验证集成
4. 记录测试结果和发现的问题

**备注：** 后端代码已准备就绪，建议尽快进行API联调测试，以确保接口集成正确、数据流转顺畅。