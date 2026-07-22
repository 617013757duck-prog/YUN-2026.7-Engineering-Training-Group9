-- ============================================
-- 医疗平台数据库初始化脚本
-- 数据库: medical_platform
-- ============================================

CREATE DATABASE IF NOT EXISTS medical_platform DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medical_platform;

-- ============================================
-- 1. 用户表 (users)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL COMMENT '角色：PATIENT/MEDICAL_STAFF/FOLLOWUP_STAFF/ADMIN',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 模拟患者表 (simulated_patients)
-- ============================================
CREATE TABLE IF NOT EXISTS simulated_patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '患者ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    patient_code VARCHAR(20) UNIQUE COMMENT '患者编号',
    gender VARCHAR(10) COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    id_card VARCHAR(20) COMMENT '身份证号（脱敏）',
    address VARCHAR(255) COMMENT '地址',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    medical_history TEXT COMMENT '病史',
    allergy_history TEXT COMMENT '过敏史',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_patient_code (patient_code),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟患者表';

-- ============================================
-- 3. 就诊记录表 (visits)
-- ============================================
CREATE TABLE IF NOT EXISTS visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '就诊ID',
    visit_code VARCHAR(20) UNIQUE COMMENT '就诊编号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/SUBMITTED/PENDING_REVIEW/IN_REVIEW/COMPLETED/REJECTED',
    chief_complaint TEXT COMMENT '主诉',
    present_illness TEXT COMMENT '现病史',
    past_history TEXT COMMENT '既往史',
    risk_level VARCHAR(20) COMMENT '风险等级：LOW/MEDIUM/HIGH/CRITICAL',
    submit_time DATETIME COMMENT '提交时间',
    review_time DATETIME COMMENT '审核时间',
    reviewer_id BIGINT COMMENT '审核医生ID',
    review_comment TEXT COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_patient_id (patient_id),
    INDEX idx_status (status),
    INDEX idx_risk_level (risk_level),
    FOREIGN KEY (patient_id) REFERENCES simulated_patients(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊记录表';

-- ============================================
-- 4. 症状表 (symptoms)
-- ============================================
CREATE TABLE IF NOT EXISTS symptoms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '症状ID',
    visit_id BIGINT NOT NULL COMMENT '就诊ID',
    symptom_name VARCHAR(100) NOT NULL COMMENT '症状名称',
    symptom_code VARCHAR(20) COMMENT '症状编码',
    severity VARCHAR(20) COMMENT '严重程度：MILD/MODERATE/SEVERE',
    duration_days INT COMMENT '持续天数',
    body_part VARCHAR(50) COMMENT '身体部位',
    description TEXT COMMENT '详细描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visit_id (visit_id),
    FOREIGN KEY (visit_id) REFERENCES visits(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='症状表';

-- ============================================
-- 5. 分诊结果表 (triage_results)
-- ============================================
CREATE TABLE IF NOT EXISTS triage_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分诊ID',
    visit_id BIGINT NOT NULL UNIQUE COMMENT '就诊ID',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级',
    risk_score DECIMAL(5,2) COMMENT '风险评分',
    urgency VARCHAR(20) COMMENT '紧急程度：ROUTINE/URGENT/EMERGENCY',
    department VARCHAR(50) COMMENT '建议科室',
    reasoning TEXT COMMENT '分诊依据',
    red_flags TEXT COMMENT '红旗症状',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visit_id (visit_id),
    FOREIGN KEY (visit_id) REFERENCES visits(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分诊结果表';

-- ============================================
-- 6. 医疗指南表 (medical_guidelines)
-- ============================================
CREATE TABLE IF NOT EXISTS medical_guidelines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '指南ID',
    guideline_code VARCHAR(20) UNIQUE COMMENT '指南编号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    category VARCHAR(50) COMMENT '分类',
    source VARCHAR(100) COMMENT '来源',
    version VARCHAR(20) COMMENT '版本',
    publish_date DATE COMMENT '发布日期',
    content TEXT COMMENT '内容',
    keywords VARCHAR(500) COMMENT '关键词',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗指南表';

-- ============================================
-- 7. 随访计划表 (followup_plans)
-- ============================================
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
    INDEX idx_status (status),
    FOREIGN KEY (patient_id) REFERENCES simulated_patients(id),
    FOREIGN KEY (visit_id) REFERENCES visits(id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访计划表';

-- ============================================
-- 8. 随访任务表 (followup_tasks)
-- ============================================
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
    INDEX idx_scheduled_date (scheduled_date),
    FOREIGN KEY (plan_id) REFERENCES followup_plans(id),
    FOREIGN KEY (executor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访任务表';

-- ============================================
-- 9. 安全告警表 (safety_alerts)
-- ============================================
CREATE TABLE IF NOT EXISTS safety_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '告警ID',
    alert_type VARCHAR(50) NOT NULL COMMENT '告警类型：OVERDUE_DIAGNOSIS/PROMPT_ATTACK/PRIVACY_LEAK',
    severity VARCHAR(20) COMMENT '严重程度：LOW/MEDIUM/HIGH/CRITICAL',
    visit_id BIGINT COMMENT '关联就诊ID',
    content TEXT COMMENT '告警内容',
    source VARCHAR(100) COMMENT '来源',
    status VARCHAR(20) DEFAULT 'UNRESOLVED' COMMENT '状态：UNRESOLVED/RESOLVED/IGNORED',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_result TEXT COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_alert_type (alert_type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    FOREIGN KEY (visit_id) REFERENCES visits(id),
    FOREIGN KEY (handler_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全告警表';

-- ============================================
-- 10. Agent 运行记录表 (agent_runs)
-- ============================================
CREATE TABLE IF NOT EXISTS agent_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '运行ID',
    run_code VARCHAR(20) UNIQUE COMMENT '运行编号',
    visit_id BIGINT COMMENT '关联就诊ID',
    agent_type VARCHAR(50) COMMENT 'Agent类型：SYMPTOM_ANALYZER/RISK_JUDGE/SUGGESTION_GENERATOR',
    model_name VARCHAR(100) COMMENT '模型名称',
    model_version VARCHAR(20) COMMENT '模型版本',
    prompt_version VARCHAR(20) COMMENT 'Prompt版本',
    input_text TEXT COMMENT '输入文本',
    output_text TEXT COMMENT '输出文本',
    processing_time INT COMMENT '处理时间（毫秒）',
    status VARCHAR(20) COMMENT '状态：SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visit_id (visit_id),
    INDEX idx_agent_type (agent_type),
    FOREIGN KEY (visit_id) REFERENCES visits(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent运行记录表';

-- ============================================
-- 11. 引用记录表 (citations)
-- ============================================
CREATE TABLE IF NOT EXISTS citations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '引用ID',
    visit_id BIGINT NOT NULL COMMENT '就诊ID',
    agent_run_id BIGINT COMMENT 'Agent运行ID',
    guideline_id BIGINT COMMENT '指南ID',
    citation_text TEXT COMMENT '引用文本',
    source_section VARCHAR(100) COMMENT '来源章节',
    relevance_score DECIMAL(5,2) COMMENT '相关度评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_visit_id (visit_id),
    INDEX idx_guideline_id (guideline_id),
    FOREIGN KEY (visit_id) REFERENCES visits(id),
    FOREIGN KEY (agent_run_id) REFERENCES agent_runs(id),
    FOREIGN KEY (guideline_id) REFERENCES medical_guidelines(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引用记录表';

-- ============================================
-- 12. 审计日志表 (audit_logs)
-- ============================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_code INT COMMENT '响应码',
    response_time INT COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ============================================
-- 初始化管理员账户
-- ============================================
INSERT INTO users (username, password, real_name, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKfNPvZ3Xe7MB4ZbCvqDJfPVlU..', '系统管理员', 'ADMIN', 1);

-- 密码为: admin123（使用 BCrypt 加密）

-- ============================================
-- 初始化测试数据（可选）
-- ============================================
-- 此处可以添加一些测试数据，但必须使用合成病例