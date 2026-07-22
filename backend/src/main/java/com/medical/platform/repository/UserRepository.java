package com.medical.platform.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.platform.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Repository
 * 
 * @author 成员B
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
}