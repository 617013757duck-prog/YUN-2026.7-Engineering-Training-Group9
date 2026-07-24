package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Repository
 *
 * @author 贺孟缘
 */
@Mapper
public interface AuditLogRepository extends BaseMapper<AuditLog> {
}