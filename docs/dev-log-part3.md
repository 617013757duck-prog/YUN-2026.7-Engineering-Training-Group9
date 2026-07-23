# 第三部分开发日志：规则引擎和AI分析模块

**开发者：** 贺孟缘
**开发时间：** 2026-07-23
**开发内容：** 规则引擎模块、AI分析集成、安全警报系统

---

## 一、开发概述

第三部分完成了医疗安全平台的规则引擎模块，包括红旗症状识别、禁忌条件检查、敏感信息脱敏等核心功能，并与AI分析流程深度集成，实现了自动化的安全风险评估机制。

## 二、新增文件列表

### 1. 实体类（Entity）
- `MedicalGuideline.java` - 医疗指南实体类
- `SafetyAlert.java` - 安全警报实体类

### 2. 数据访问层（Repository）
- `MedicalGuidelineRepository.java` - 医疗指南Repository
- `SafetyAlertRepository.java` - 安全警报Repository

### 3. 规则引擎核心（Rule）
- `RedFlagRuleEngine.java` - 红旗症状规则引擎
  - 支持30+种红旗症状关键词识别
  - 包含心血管、呼吸、神经、消化等多系统红旗症状
  - 自动评估严重程度和紧急程度

- `ContraindicationChecker.java` - 禁忌条件检查器
  - 药物过敏检查（青霉素、阿司匹林等11种常见过敏药物）
  - 特殊人群识别（孕妇、儿童、老年人、肝肾功能不全等）
  - 年龄相关禁忌检查
  - 病史禁忌症检查（消化道溃疡、哮喘、青光眼等）

### 4. 工具类（Utils）
- `DesensitizationUtil.java` - 敏感信息脱敏工具
  - 姓名脱敏：张三 → 张*
  - 身份证脱敏：123456789012345678 → 123456********5678
  - 手机号脱敏：13812345678 → 138****5678
  - 银行卡、邮箱、地址等敏感信息脱敏

### 5. 业务层（Service）
- `RuleService.java` - 规则引擎服务
  - 整合红旗症状和禁忌条件检查
  - 自动创建安全警报
  - 更新就诊记录风险等级
  - 提供安全警报管理功能

### 6. 控制层（Controller）
- `RuleController.java` - 规则引擎控制器
  - POST `/api/rules/safety-check/{visitId}` - 执行安全检查
  - GET `/api/rules/alerts/{visitId}` - 获取安全警报列表
  - PUT `/api/rules/alerts/{alertId}/handle` - 处理安全警报

## 三、核心功能实现

### 3.1 红旗症状规则引擎

**实现原理：**
- 建立红旗症状关键词映射表，包含症状关键词、严重程度、可能疾病、紧急程度
- 对症状名称和描述进行关键词匹配
- 自动标记红旗症状并评估风险等级

**支持的红旗症状分类：**
1. **心血管系统：** 胸痛、胸闷、心悸、心前区疼痛
2. **呼吸系统：** 呼吸困难、气促、咯血、咳血
3. **神经系统：** 剧烈头痛、突发头痛、意识模糊、昏迷、肢体麻木、口角歪斜
4. **消化系统：** 呕血、便血、黑便、剧烈腹痛、急腹症
5. **眼科急症：** 突发视力丧失、视力模糊、视野缺损
6. **全身性症状：** 持续高烧、高热、寒战、休克
7. **过敏急症：** 过敏反应、严重过敏、药物过敏

### 3.2 禁忌条件检查器

**检查维度：**
1. **药物过敏检查：**
   - 识别常见过敏药物（青霉素、阿莫西林、头孢、磺胺、阿司匹林、布洛芬、碘造影剂、链霉素、庆大霉素、环丙沙星、万古霉素）
   - 生成高级别警告和建议

2. **特殊人群识别：**
   - 妊娠期、哺乳期患者
   - 婴幼儿（≤3岁）、儿童
   - 老年患者（≥65岁）
   - 肝肾功能异常患者
   - 心脏病、高血压、糖尿病患者

3. **年龄相关禁忌：**
   - 儿童禁忌药物提醒（喹诺酮类、四环素类）
   - 老年人用药安全提醒（易致跌倒、意识模糊的药物）

4. **病史禁忌症检查：**
   - 消化道溃疡：慎用NSAIDs类药物
   - 哮喘：慎用β受体阻滞剂
   - 青光眼：慎用抗胆碱能药物、扩瞳药物

### 3.3 AI分析流程集成

**流程优化：**
```
1. 执行安全检查（红旗症状 + 禁忌条件）
   ↓
2. 调用AI服务进行症状分析
   ↓
3. 整合规则引擎和AI分析的风险评估
   ↓
4. 更新就诊记录状态和风险等级
   ↓
5. 保存分诊结果
```

**风险等级决策逻辑：**
- 规则引擎发现高风险 → 最终风险等级为"高风险"
- AI分析和规则引擎均为中风险 → 最终风险等级为"中风险"
- 无安全问题和低风险AI分析 → 最终风险等级为"低风险"

### 3.4 安全警报系统

**警报类型：**
- 红旗症状警报：包含症状名称、关键词、可能疾病、建议措施
- 禁忌条件警报：包含禁忌类型、具体内容、用药建议

**警报状态：**
- 状态0：未处理
- 状态1：已处理（记录处理人和处理结果）

## 四、数据库设计

### 4.1 safety_alerts表结构

```sql
CREATE TABLE `safety_alerts` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `visit_id` BIGINT NOT NULL COMMENT '就诊ID',
  `alert_type` VARCHAR(50) NOT NULL COMMENT '警报类型',
  `alert_level` VARCHAR(20) NOT NULL COMMENT '警报级别（高/中/低）',
  `alert_content` TEXT NOT NULL COMMENT '警报内容',
  `trigger_source` VARCHAR(50) COMMENT '触发来源（RedFlagRuleEngine/ContraindicationChecker）',
  `status` INT DEFAULT 0 COMMENT '状态（0-未处理，1-已处理）',
  `handled_by` VARCHAR(100) COMMENT '处理人',
  `handle_result` TEXT COMMENT '处理结果',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除标记',
  INDEX `idx_visit_id` (`visit_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全警报表';
```

### 4.2 medical_guidelines表结构

```sql
CREATE TABLE `medical_guidelines` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '指南标题',
  `category` VARCHAR(50) COMMENT '指南分类',
  `content` TEXT COMMENT '指南内容',
  `source` VARCHAR(100) COMMENT '来源',
  `version` VARCHAR(20) COMMENT '版本',
  `status` INT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '逻辑删除标记',
  INDEX `idx_category` (`category`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗指南表';
```

## 五、API接口文档

### 5.1 执行安全检查

**接口：** `POST /api/rules/safety-check/{visitId}`

**权限：** MEDICAL_STAFF, ADMIN

**响应示例：**
```json
{
  "code": 200,
  "message": "安全检查完成",
  "data": {
    "visitId": 123,
    "hasSafetyIssue": true,
    "redFlagResult": {
      "hasRedFlag": true,
      "redFlagCount": 1,
      "maxRiskLevel": "高",
      "redFlagMatches": [
        {
          "symptomId": 456,
          "symptomName": "胸痛",
          "keyword": "胸痛",
          "severity": "高",
          "possibleDisease": "心血管疾病",
          "urgency": "立即就医"
        }
      ]
    },
    "contraindicationResult": {
      "hasContraindication": false,
      "warningCount": 0,
      "warnings": []
    }
  }
}
```

### 5.2 获取安全警报列表

**接口：** `GET /api/rules/alerts/{visitId}`

**权限：** MEDICAL_STAFF, ADMIN, FOLLOWUP_STAFF

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "visitId": 123,
      "alertType": "红旗症状",
      "alertLevel": "高",
      "alertContent": "发现红旗症状: 胸痛 (关键词: 胸痛), 可能疾病: 心血管疾病, 建议: 立即就医",
      "triggerSource": "RedFlagRuleEngine",
      "status": 0,
      "createTime": "2026-07-23T09:00:00"
    }
  ]
}
```

### 5.3 处理安全警报

**接口：** `PUT /api/rules/alerts/{alertId}/handle`

**权限：** MEDICAL_STAFF, ADMIN

**请求参数：**
- `handledBy`: 处理人姓名
- `handleResult`: 处理结果说明

## 六、测试结果

### 6.1 编译测试
```
[INFO] BUILD SUCCESS
[INFO] Total time:  3.296 s
[INFO] Compiling 51 source files
```

**结果：** 编译成功，无错误

### 6.2 功能测试要点
1. 红旗症状识别准确性测试
2. 禁忌条件检查完整性测试
3. 敏感信息脱敏正确性测试
4. AI分析流程集成测试
5. 安全警报创建和管理测试

## 七、项目进度

### 已完成：
- ✅ 第一部分：基础架构完善（用户管理、JWT认证、配置类）
- ✅ 第二部分：就诊业务核心功能（实体类、状态机、Repository、Service、Controller）
- ✅ 第三部分：规则引擎和AI分析模块

### 待开发：
- ⏳ 第四部分：随访管理模块
- ⏳ 第五部分：系统测试和文档完善

## 八、技术亮点

1. **规则引擎设计：** 采用关键词映射表实现灵活的规则配置，支持动态扩展
2. **多层风险决策：** 规则引擎和AI分析协同工作，提高风险识别准确性
3. **自动化警报：** 发现安全问题时自动创建警报，降低人工干预成本
4. **数据安全：** 敏感信息脱敏工具保护患者隐私
5. **流程整合：** 规则检查无缝集成到AI分析流程，实现一站式安全评估

## 九、后续优化建议

1. **规则配置化：** 将红旗症状和禁忌条件从代码迁移到数据库，支持动态配置
2. **规则权重：** 为不同规则设置权重，实现更精细的风险评分
3. **机器学习：** 基于历史数据训练红旗症状识别模型，提高准确性
4. **实时监控：** 增加安全警报的实时推送机制（WebSocket）
5. **审计日志：** 记录所有规则检查的详细日志，便于追溯和分析

---

**备注：** 本部分代码已编译通过，准备提交到GitHub仓库。