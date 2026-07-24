package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.AgentRun;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent运行记录 Repository
 *
 * @author 贺孟缘
 */
@Mapper
public interface AgentRunRepository extends BaseMapper<AgentRun> {
}