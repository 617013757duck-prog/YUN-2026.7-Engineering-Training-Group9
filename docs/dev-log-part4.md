# 第四部分开发日志：随访管理模块

**开发者：** 贺孟缘  
**开发时间：** 2026-07-23  
**开发内容：** 随访计划管理、随访任务管理、自动任务生成

---

## 一、开发概述

第四部分完成了基层医疗平台的随访管理模块，包括随访计划的创建与管理、随访任务的调度与执行、自动任务生成等核心功能，为慢病管理和术后随访提供了完整的信息化解决方案。

## 二、新增文件列表

### 1. 实体类（Entity）
- [FollowupPlan.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/entity/FollowupPlan.java) - 随访计划实体类
- [FollowupTask.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/entity/FollowupTask.java) - 随访任务实体类

### 2. 数据访问层（Repository）
- [FollowupPlanRepository.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/repository/FollowupPlanRepository.java) - 随访计划Repository
- [FollowupTaskRepository.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/repository/FollowupTaskRepository.java) - 随访任务Repository

### 3. 数据传输对象（DTO）
- [FollowupPlanRequest.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/dto/FollowupPlanRequest.java) - 随访计划请求DTO
- [FollowupTaskCompleteRequest.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/dto/FollowupTaskCompleteRequest.java) - 任务完成请求DTO

### 4. 业务层（Service）
- [FollowupService.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/service/FollowupService.java) - 随访管理服务

### 5. 控制层（Controller）
- [FollowupController.java](file:///d:/IdeaProjects/2026年人工智能工程实训/结业实训/backend/src/main/java/com/medical/platform/controller/FollowupController.java) - 随访管理控制器

---

## 三、核心功能实现

### 3.1 随访计划管理

**功能特性：**
- 创建随访计划（支持慢性病、术后、健康监测三种类型）
- 更新随访计划（名称、结束日期、频率、状态）
- 查询患者的随访计划列表
- 删除随访计划（软删除，同时删除关联任务）

**计划类型：**
- `CHRONIC_DISEASE` - 慢性病随访
- `POST_OPERATION` - 术后随访
- `HEALTH_MONITORING` - 健康监测

**计划状态：**
- `ACTIVE` - 活跃
- `PAUSED` - 暂停
- `COMPLETED` - 已完成
- `CANCELLED` - 已取消

**频率设置：**
- `DAILY` - 每日随访
- `WEEKLY` - 每周随访
- `MONTHLY` - 每月随访

### 3.2 随访任务管理

**功能特性：**
- 创建随访任务（手动创建）
- 完成随访任务（记录执行人、结果、备注）
- 查询随访计划的任务列表
- 查询今日待执行任务
- 查询所有待执行任务

**任务状态：**
- `PENDING` - 待执行
- `COMPLETED` - 已完成
- `OVERDUE` - 已逾期
- `CANCELLED` - 已取消

### 3.3 自动任务生成

**实现原理：**
- 根据随访计划的频率自动计算下一个任务日期
- 生成任务编号（FT + 时间戳）
- 设置默认任务内容
- 验证计划状态和结束日期

**生成逻辑：**
```
每日随访 → nextDate = today + 1天
每周随访 → nextDate = today + 1周
每月随访 → nextDate = today + 1月
```

---

## 四、API接口文档

### 4.1 随访计划接口

#### 创建随访计划
**接口：** `POST /api/followup/plans`  
**权限：** FOLLOWUP_STAFF, ADMIN, MEDICAL_STAFF

**请求示例：**
```json
{
  "patientId": 1,
  "visitId": 123,
  "planName": "高血压慢病随访计划",
  "planType": "CHRONIC_DISEASE",
  "startDate": "2026-07-23",
  "endDate": "2026-12-31",
  "frequency": "MONTHLY",
  "creatorId": 2
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "随访计划创建成功",
  "data": {
    "id": 1,
    "planCode": "FP20260723092600000",
    "patientId": 1,
    "status": "ACTIVE",
    "createTime": "2026-07-23T09:26:00"
  }
}
```

#### 获取患者随访计划列表
**接口：** `GET /api/followup/plans/patient/{patientId}`  
**权限：** FOLLOWUP_STAFF, ADMIN, MEDICAL_STAFF

#### 更新随访计划
**接口：** `PUT /api/followup/plans/{id}`  
**权限：** FOLLOWUP_STAFF, ADMIN

#### 删除随访计划
**接口：** `DELETE /api/followup/plans/{id}`  
**权限：** ADMIN

### 4.2 随访任务接口

#### 创建随访任务
**接口：** `POST /api/followup/tasks`  
**权限：** FOLLOWUP_STAFF, ADMIN

**请求示例：**
```json
{
  "planId": 1,
  "scheduledDate": "2026-08-23",
  "taskContent": "定期随访"
}
```

#### 完成随访任务
**接口：** `PUT /api/followup/tasks/{id}/complete`  
**权限：** FOLLOWUP_STAFF, ADMIN

**请求示例：**
```json
{
  "executorId": 5,
  "result": "患者情况良好，血压控制在正常范围",
  "notes": "建议继续保持良好的生活习惯"
}
```

#### 获取今日随访任务
**接口：** `GET /api/followup/tasks/today`  
**权限：** FOLLOWUP_STAFF, ADMIN

#### 获取待执行的随访任务
**接口：** `GET /api/followup/tasks/pending`  
**权限：** FOLLOWUP_STAFF, ADMIN

#### 自动生成随访任务
**接口：** `POST /api/followup/tasks/generate/{planId}`  
**权限：** FOLLOWUP_STAFF, ADMIN

---

## 五、数据库设计

### 5.1 followup_plans表结构

```sql
CREATE TABLE IF NOT EXISTS followup_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '计划ID',
    plan_code VARCHAR(20) UNIQUE COMMENT '计划编号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    visit_id BIGINT COMMENT '关联就诊ID',
    plan_name VARCHAR(100) COMMENT '计划名称',
    plan_type VARCHAR(20) COMMENT '类型：CHRONIC_DISEASE/POST_OPERATION/HEALTH_MONITORING',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    frequency VARCHAR(20) COMMENT '频率：DAILY/WEEKLY/MONTHLY',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/PAUSED/COMPLETED/CANCELLED',
    creator_id BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_patient_id (patient_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访计划表';
```

### 5.2 followup_tasks表结构

```sql
CREATE TABLE IF NOT EXISTS followup_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_code VARCHAR(20) UNIQUE COMMENT '任务编号',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    scheduled_date DATE COMMENT '计划日期',
    actual_date DATE COMMENT '实际执行日期',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/COMPLETED/OVERDUE/CANCELLED',
    executor_id BIGINT COMMENT '执行人ID',
    task_content TEXT COMMENT '任务内容',
    result TEXT COMMENT '执行结果',
    notes TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plan_id (plan_id),
    INDEX idx_status (status),
    INDEX idx_scheduled_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访任务表';
```

---

## 六、编译测试结果

### 6.1 编译测试
```
[INFO] BUILD SUCCESS
[INFO] Total time:  3.248 s
[INFO] Compiling 60 source files
```

**结果：** ✅ 编译成功，无错误

### 6.2 功能测试要点
1. 随访计划创建和管理测试
2. 随访任务创建和完成测试
3. 自动任务生成逻辑测试
4. 权限控制测试
5. 数据验证测试

---

## 七、技术亮点

1. **自动任务生成：** 根据随访频率自动计算下一个任务日期
2. **灵活的频率设置：** 支持每日、每周、每月多种随访频率
3. **完整的生命周期管理：** 从计划创建到任务完成的完整流程
4. **权限控制：** 细粒度的角色权限管理（FOLLOWUP_STAFF、MEDICAL_STAFF、ADMIN）
5. **数据一致性：** 使用事务保证计划和任务的原子操作

---

## 八、与已有模块的协调

### 8.1 与就诊模块协调
- 随访计划可以关联就诊记录（visit_id）
- 从就诊记录自动生成随访计划（待实现）

### 8.2 与用户模块协调
- 随访人员角色（FOLLOWUP_STAFF）
- 执行人记录（executor_id）
- 创建人记录（creator_id）

### 8.3 与安全模块协调
- JWT认证保护所有接口
- 角色权限控制访问
- 敏感数据保护

---

## 九、项目进度

### 已完成：
- ✅ 第一部分：基础架构完善
- ✅ 第二部分：就诊业务核心功能
- ✅ 第三部分：规则引擎和AI分析模块
- ✅ 第四部分：随访管理模块

### 待开发：
- ⏳ 第五部分：系统测试和文档完善

---

## 十、后续优化建议

1. **定时任务调度：** 使用Quartz或Spring Task实现自动任务生成
2. **通知提醒：** 集成短信、邮件、APP推送等通知方式
3. **统计分析：** 随访完成率、患者依从性等数据分析
4. **移动端支持：** 随访人员移动端应用（APP/小程序）
5. **智能推荐：** 基于患者情况推荐随访频率和内容

---

**备注：** 本部分代码已编译通过，准备提交到GitHub仓库。随访管理模块为慢病管理和术后随访提供了完整的信息化支持。