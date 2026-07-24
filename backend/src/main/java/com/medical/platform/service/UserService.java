package com.medical.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.platform.dto.*;
import com.medical.platform.entity.User;
import com.medical.platform.exception.BusinessException;
import com.medical.platform.repository.UserRepository;
import com.medical.platform.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户 Service
 * 
 * @author 成员B
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户名或密码错误");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR.getCode(), "用户名或密码错误");
        }
        
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED.getCode(), "账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", user.getUsername());
        
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                token
        );
    }
    
    /**
     * 用户注册
     */
    public UserVO register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userRepository.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS.getCode(), "用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(1);
        
        userRepository.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
        
        return convertToVO(user);
    }
    
    /**
     * 更新用户信息
     */
    public UserVO updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        userRepository.updateById(user);
        log.info("用户信息更新成功: id={}", id);
        
        return convertToVO(user);
    }
    
    /**
     * 根据 ID 获取用户
     */
    public UserVO getUserVOById(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        return convertToVO(user);
    }
    
    /**
     * 分页查询用户
     */
    public PageResult<UserVO> getUserPage(Long current, Long size, String role, String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> result = userRepository.selectPage(page, wrapper);
        
        return PageResult.of(
                result.getTotal(),
                result.getPages(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords().stream().map(this::convertToVO).toList()
        );
    }
    
    /**
     * 删除用户（逻辑删除）
     */
    public void deleteUser(Long id) {
        User user = userRepository.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        userRepository.deleteById(id);
        log.info("用户删除成功: id={}", id);
    }
    
    /**
     * 根据 ID 获取用户实体
     */
    public User getUserById(Long id) {
        return userRepository.selectById(id);
    }
    
    /**
     * 根据用户名获取用户实体
     */
    public User getUserByUsername(String username) {
        return userRepository.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
    }
    
    /**
     * 根据用户名获取用户VO（用于前端接口）
     */
    public UserVO getUserVOByUsername(String username) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        return convertToVO(user);
    }
    
    /**
     * 转换为 VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}