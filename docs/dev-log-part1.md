# 后端开发日志 - 第一部分：基础架构完善

**开发人员：** 成员B（后端开发）
**开发日期：** 2026-07-22
**分支：** backend-dev
**状态：** ✅ 已完成

---

## 一、开发目标

完善 Spring Boot 后端基础架构，为后续业务模块开发打好基础。包括：
- 用户认证模块（JWT + Spring Security）
- MyBatis Plus 配置（分页、自动填充、逻辑删除）
- API 文档配置（Knife4j）
- 跨域配置（CORS）
- 统一响应格式和错误码
- 用户管理接口（注册、查询、修改、删除）

---

## 二、开发内容

### 2.1 基础配置类

| 文件 | 说明 |
|------|------|
| `config/MybatisPlusConfig.java` | MyBatis Plus 配置：分页插件、自动填充处理器 |
| `config/Knife4jConfig.java` | API 文档配置：OpenAPI 信息 |
| `config/CorsConfig.java` | 跨域配置：允许所有域名、方法、头 |
| `config/SecurityConfig.java` | Spring Security 配置：JWT 无状态认证、接口权限控制 |

### 2.2 安全模块

| 文件 | 说明 |
|------|------|
| `security/JwtAuthenticationFilter.java` | JWT 认证过滤器：解析 Token、设置认证信息 |
| `utils/JwtUtil.java` | JWT 工具类：生成、解析、验证 Token |
| `utils/SecurityUtils.java` | 安全工具类：获取当前登录用户信息 |

### 2.3 异常处理

| 文件 | 说明 |
|------|------|
| `exception/BusinessException.java` | 业务异常类 |
| `exception/GlobalExceptionHandler.java` | 全局异常处理器 |

### 2.4 DTO 类

| 文件 | 说明 |
|------|------|
| `dto/Result.java` | 统一响应类 |
| `dto/PageResult.java` | 分页响应类 |
| `dto/ErrorCode.java` | 错误码枚举 |
| `dto/LoginRequest.java` | 登录请求 DTO |
| `dto/LoginResponse.java` | 登录响应 DTO |
| `dto/UserRegisterRequest.java` | 用户注册请求 DTO |
| `dto/UserUpdateRequest.java` | 用户信息修改请求 DTO |
| `dto/UserVO.java` | 用户视图对象（不含密码） |

### 2.5 用户管理模块

| 文件 | 说明 |
|------|------|
| `entity/User.java` | 用户实体类 |
| `repository/UserRepository.java` | 用户数据访问层 |
| `service/UserService.java` | 用户业务逻辑层 |
| `controller/AuthController.java` | 认证控制器（登录、注销） |
| `controller/UserController.java` | 用户管理控制器（CRUD） |
| `controller/HealthController.java` | 健康检查控制器 |

---

## 三、API 接口清单

### 3.1 认证接口（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户注销 |

### 3.2 用户管理接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/users/register` | 公开 | 用户注册 |
| GET | `/api/users` | ADMIN | 分页查询用户 |
| GET | `/api/users/{id}` | ADMIN | 获取用户详情 |
| PUT | `/api/users/{id}` | ADMIN | 更新用户信息 |
| DELETE | `/api/users/{id}` | ADMIN | 删除用户 |

### 3.3 健康检查接口（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/health` | 健康检查 |

---

## 四、数据库表

第一部分使用以下表（已在 `database/init.sql` 中创建）：

- `users` - 用户表

---

## 五、测试说明

### 5.1 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 5.2 配置修改

1. 修改 `backend/src/main/resources/application.yml`：
   - 修改 MySQL 密码：`spring.datasource.password`
   - 修改 JWT 密钥：`jwt.secret`（生产环境必须修改）

2. 初始化数据库：
   ```bash
   mysql -u root -p < database/init.sql
   ```

3. 默认管理员账户：
   - 用户名：`admin`
   - 密码：`admin123`

### 5.3 启动项目

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

或使用 IntelliJ IDEA 直接运行 `MedicalPlatformApplication.java`。

### 5.4 测试接口

1. **API 文档地址：** http://localhost:8080/doc.html
2. **健康检查：** GET http://localhost:8080/api/health
3. **用户登录：** POST http://localhost:8080/api/auth/login
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
4. **用户注册：** POST http://localhost:8080/api/users/register
   ```json
   {
     "username": "doctor01",
     "password": "123456",
     "realName": "张医生",
     "phone": "13800138000",
     "email": "doctor@hospital.com",
     "role": "MEDICAL_STAFF"
   }
   ```

---

## 六、待解决问题

1. **JDK 和 Maven 未安装：** 系统未安装 JDK 17+ 和 Maven，需要安装后才能编译运行
2. **MySQL 密码未配置：** 需要修改 `application.yml` 中的数据库密码
3. **单元测试未编写：** 第一部分暂未编写单元测试，后续补充

---

## 七、下一部分计划

### 第二部分：就诊业务核心功能
- 就诊实体类和 DTO（Visit、Symptom、TriageResult）
- 就诊状态机（DRAFT → SUBMITTED → PENDING_REVIEW → IN_REVIEW → COMPLETED/REJECTED）
- 就诊业务接口（创建、提交、查询、审核）
- 症状管理（添加、查询、结构化）

---

## 八、提交记录

- **commit:** feat: 实现用户认证模块（JWT + Spring Security）
- **commit:** feat: 第一部分基础架构完善（配置、用户管理、安全）
- **分支:** backend-dev
- **推送状态:** ✅ 已推送到 GitHub
