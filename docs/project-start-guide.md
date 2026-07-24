# 医疗平台项目启动指南

## 一、环境准备检查

### 1. MySQL数据库

**问题：** MySQL命令未配置到系统PATH

**解决方案：**

#### 方案1：使用完整路径（推荐）

找到MySQL安装路径，常见位置：
- `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`
- `C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe`
- `C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe`

**运行数据库初始化：**
```powershell
# 方法1：先登录MySQL，再导入脚本
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p

# 在MySQL命令行中执行：
source D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend\src\main\resources\schema.sql;

# 方法2：直接导入（一条命令）
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p medical_platform < "D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend\src\main\resources\schema.sql"
```

**输入密码：** `07040528hmy`

#### 方案2：配置MySQL到PATH

1. 右键"此电脑" → 属性 → 高级系统设置 → 环境变量
2. 在"系统变量"中找到"Path"，点击编辑
3. 添加MySQL的bin目录路径，例如：`C:\Program Files\MySQL\MySQL Server 8.0\bin`
4. 重启Powerhell，即可直接使用mysql命令

---

### 2. Redis数据库

**问题：** redis-server命令未找到

**解决方案：**

#### 方案1：检查Redis是否安装

Redis在Windows上需要单独安装，下载地址：
- GitHub: https://github.com/microsoftarchive/redis/releases
- 或使用Chocolatey: `choco install redis-64`

#### 方案2：使用Memurai（Windows版Redis替代）

Memurai是Windows原生Redis兼容服务：
- 下载：https://www.memurai.com/get-memurai
- 安装后会自动添加到PATH
- 启动命令：`memurai-server`

#### 方案3：暂时禁用Redis（仅用于测试）

如果只是测试，可以暂时禁用Redis依赖：

修改 `backend/src/main/resources/application.yml`：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      timeout: 5000
```

如果Redis未启动，需要：
1. 注释掉Redis相关配置
2. 暂时禁用Redis相关的功能（如缓存）

---

## 二、正确启动流程

### 2.1 数据库初始化

**步骤1：启动MySQL服务**
- Windows服务中找到MySQL，确保已启动
- 或使用命令：`net start MySQL80`（根据版本调整）

**步骤2：初始化数据库**
```powershell
# 使用完整路径
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < "D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend\src\main\resources\schema.sql"

# 输入密码：07040528hmy
```

**步骤3：验证数据库创建成功**
```powershell
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SHOW DATABASES LIKE 'medical_platform';"
```

---

### 2.2 启动Redis（如果已安装）

**如果安装了Redis：**
```powershell
redis-server
# 或使用Memurai
memurai-server
```

**如果未安装：**
- 暂时跳过Redis
- 或安装Memurai（推荐）

---

### 2.3 启动AI服务（已启动）

AI服务已在运行：
- 地址：http://localhost:8000
- 状态：✅ 正常

测试：
```powershell
curl http://localhost:8000/api/ai/health
```

---

### 2.4 启动后端服务

**步骤1：确保数据库已初始化**

**步骤2：启动后端**
```powershell
cd D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend
mvn spring-boot:run
```

**预期输出：**
```
Started MedicalPlatformApplication in X seconds
```

**访问API文档：**
http://localhost:8080/doc.html

---

### 2.5 启动前端服务

**步骤1：安装依赖**
```powershell
cd D:\IdeaProjects\2026年人工智能工程实训\结业实训\frontend
npm install
```

**步骤2：启动开发服务器**
```powershell
npm run dev
```

**预期输出：**
```
  VITE v5.2.0  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

**访问前端：**
http://localhost:5173

---

## 三、常见问题解决

### 3.1 MySQL连接失败

**错误：** `Communications link failure`

**解决：**
1. 检查MySQL服务是否启动
2. 检查端口3306是否被占用
3. 验证用户名密码：root / 07040528hmy

---

### 3.2 Redis连接失败

**错误：** `Unable to connect to Redis`

**解决：**
1. 安装Redis或Memurai
2. 或暂时禁用Redis配置

---

### 3.3 前端无法连接后端

**错误：** `Network Error`

**解决：**
1. 确认后端已启动（端口8080）
2. 检查CORS配置
3. 确认API地址正确

---

## 四、快速启动脚本

### 4.1 一键启动（假设MySQL已配置）

```powershell
# 启动数据库（新窗口）
Start-Process "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -ArgumentList "-u root -p"

# 启动Redis（如果已安装，新窗口）
Start-Process redis-server

# 启动AI服务（已启动）
# 无需操作

# 启动后端（新窗口）
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend; mvn spring-boot:run"

# 启动前端（新窗口）
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd D:\IdeaProjects\2026年人工智能工程实训\结业实训\frontend; npm install; npm run dev"
```

---

## 五、数据库连接信息

**MySQL配置：**
- Host: localhost
- Port: 3306
- Database: medical_platform
- Username: root
- Password: 07040528hmy

**Redis配置（如果需要）：**
- Host: localhost
- Port: 6379
- Password: (空)

---

## 六、测试账号

**管理员：**
- 用户名：admin
- 密码：admin123

**医生：**
- 用户名：doctor
- 密码：doctor123

**随访员：**
- 用户名：followup
- 密码：followup123

---

**注意：** 如果MySQL和Redis未安装或未配置PATH，请先安装或使用完整路径运行命令。