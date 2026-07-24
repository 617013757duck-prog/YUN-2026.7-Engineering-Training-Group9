-- 医疗平台数据库初始化脚本（简化版）
USE medical_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL COMMENT '角色（ADMIN/MEDICAL_STAFF/FOLLOWUP_STAFF）',
    status INT DEFAULT 1 COMMENT '状态（0禁用 1正常）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除（0未删除 1已删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 就诊记录表
CREATE TABLE IF NOT EXISTS visits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '就诊ID',
    patient_name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    patient_phone VARCHAR(20) COMMENT '患者手机号',
    patient_id_card VARCHAR(18) COMMENT '患者身份证号',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别',
    chief_complaint TEXT COMMENT '主诉',
    history_of_present_illness TEXT COMMENT '现病史',
    past_medical_history TEXT COMMENT '既往史',
    medication_history TEXT COMMENT '用药史',
    allergies TEXT COMMENT '过敏史',
    status VARCHAR(50) NOT NULL COMMENT '就诊状态',
    risk_level VARCHAR(20) COMMENT '风险等级',
    risk_basis TEXT COMMENT '风险依据',
    ai_analysis TEXT COMMENT 'AI分析结果',
    medical_staff_comment TEXT COMMENT '医务人员备注',
    triage_result TEXT COMMENT '分诊结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊记录表';

-- 症状表
CREATE TABLE IF NOT EXISTS symptoms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '症状ID',
    visit_id BIGINT NOT NULL COMMENT '就诊ID',
    symptom_name VARCHAR(100) NOT NULL COMMENT '症状名称',
    symptom_code VARCHAR(50) COMMENT '症状编码',
    severity INT COMMENT '严重程度（1-10）',
    onset_time DATETIME COMMENT '发作时间',
    duration VARCHAR(50) COMMENT '持续时间',
    location VARCHAR(100) COMMENT '部位',
    description TEXT COMMENT '描述',
    associated_symptoms TEXT COMMENT '伴随症状',
    is_red_flag INT DEFAULT 0 COMMENT '是否红旗症状（0否 1是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_visit_id (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='症状表';

-- 分诊结果表
CREATE TABLE IF NOT EXISTS triage_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分诊结果ID',
    visit_id BIGINT NOT NULL UNIQUE COMMENT '就诊ID',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级',
    recommendations TEXT COMMENT '建议',
    ai_model_version VARCHAR(50) COMMENT 'AI模型版本',
    prompt_version VARCHAR(50) COMMENT 'Prompt版本',
    knowledge_base_version VARCHAR(50) COMMENT '知识库版本',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_visit_id (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分诊结果表';

-- 医疗指南表
CREATE TABLE IF NOT EXISTS medical_guidelines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指南ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    category VARCHAR(100) COMMENT '分类',
    content TEXT COMMENT '内容',
    source VARCHAR(200) COMMENT '来源',
    version VARCHAR(50) COMMENT '版本',
    status INT DEFAULT 1 COMMENT '状态（0禁用 1正常）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗指南表';

-- 安全警报表
CREATE TABLE IF NOT EXISTS safety_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '警报ID',
    visit_id BIGINT COMMENT '就诊ID',
    alert_type VARCHAR(50) NOT NULL COMMENT '警报类型',
    alert_level VARCHAR(20) NOT NULL COMMENT '警报等级',
    alert_content TEXT NOT NULL COMMENT '警报内容',
    trigger_source VARCHAR(100) COMMENT '触发源',
    status INT DEFAULT 0 COMMENT '状态（0未处理 1已处理）',
    handler VARCHAR(50) COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_result TEXT COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_visit_id (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全警报表';

-- 随访计划表
CREATE TABLE IF NOT EXISTS followup_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    patient_name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
    plan_type VARCHAR(50) COMMENT '计划类型',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    frequency VARCHAR(50) COMMENT '随访频率',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访计划表';

-- 随访任务表
CREATE TABLE IF NOT EXISTS followup_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_date DATE COMMENT '任务日期',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    executor VARCHAR(50) COMMENT '执行人',
    execute_time DATETIME COMMENT '执行时间',
    execute_result TEXT COMMENT '执行结果',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访任务表';

-- 引用记录表
CREATE TABLE IF NOT EXISTS citations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '引用ID',
    visit_id BIGINT NOT NULL COMMENT '就诊ID',
    citation_type VARCHAR(50) COMMENT '引用类型',
    title VARCHAR(200) COMMENT '标题',
    source VARCHAR(200) COMMENT '来源',
    content TEXT COMMENT '内容',
    relevance_score FLOAT COMMENT '相关性得分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_visit_id (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引用记录表';

-- 审计日志表（用于AI服务）
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审计日志ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    user_id BIGINT COMMENT '用户ID',
    visit_id BIGINT COMMENT '就诊ID',
    details TEXT COMMENT '详细信息',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_event_type (event_type),
    INDEX idx_user_id (user_id),
    INDEX idx_visit_id (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 完成
SELECT 'Database initialization completed!' AS message;