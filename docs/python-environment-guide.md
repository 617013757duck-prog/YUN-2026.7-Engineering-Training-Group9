# Python环境配置指南

**检查时间：** 2026-07-23  
**当前系统Python版本：** Python 3.14.0  
**目标Python版本：** Python 3.10 或 Python 3.11

---

## 一、Python版本检查结果

### 1.1 已安装版本
```bash
py -0
 -V:3.14 *        Python 3.14 (64-bit)
```

**结论：** ✅ 系统仅安装了Python 3.14.0，未发现Python 3.9或3.10

### 1.2 版本检测命令
```bash
# 尝试检测Python 3.9
python3.9 --version    ❌ 未找到
py -3.9 --version      ❌ 未找到

# 尝试检测Python 3.10
python3.10 --version   ❌ 未找到
py -3.10 --version     ❌ 未找到
```

---

## 二、问题分析

### 2.1 兼容性问题
- **Python 3.14.0** 是最新版本（2024年10月发布）
- 深度学习生态系统（PyTorch、TorchVision、Transformers）尚未完全适配
- 错误：`RuntimeError: operator torchvision::nms does not exist`

### 2.2 推荐版本
- ✅ **Python 3.11** （最推荐，稳定性最佳）
- ✅ **Python 3.10** （兼容性良好）
- ✅ **Python 3.12** （支持度较好）
- ❌ **Python 3.14** （兼容性问题）

---

## 三、解决方案

### 方案一：安装Python 3.11（推荐）

#### 步骤1：下载Python 3.11
**下载地址：** https://www.python.org/downloads/release/python-3119/

**选择版本：**
- Windows installer (64-bit) - 推荐
- 文件名：python-3.11.9-amd64.exe

#### 步骤2：安装配置
1. 双击运行安装程序
2. **重要**：勾选 "Add Python to PATH"
3. 选择 "Customize installation"
4. 保持默认选项，点击 "Install"
5. 等待安装完成

#### 步骤3：验证安装
```bash
py -0
 -V:3.14 *        Python 3.14 (64-bit)
 -V:3.11          Python 3.11 (64-bit)
```

#### 步骤4：使用Python 3.11安装依赖
```bash
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\ai-service

# 使用Python 3.11
py -3.11 -m pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 启动AI服务
py -3.11 main.py
```

---

### 方案二：使用Anaconda（推荐）

#### 步骤1：下载Anaconda
**下载地址：** https://www.anaconda.com/download

**文件大小：** 约500MB

#### 步骤2：安装Anaconda
1. 双击运行安装程序
2. 选择 "Just Me"（推荐）
3. 安装路径：保持默认
4. **重要**：勾选 "Add Anaconda to my PATH environment variable"
5. 完成安装

#### 步骤3：创建虚拟环境
```bash
# 打开Anaconda Prompt（从开始菜单）

# 创建Python 3.11环境
conda create -n medical-ai python=3.11

# 激活环境
conda activate medical-ai

# 验证Python版本
python --version
# 输出：Python 3.11.x
```

#### 步骤4：安装依赖
```bash
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\ai-service

pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 启动AI服务
python main.py
```

---

### 方案三：使用Miniconda（轻量级）

#### 步骤1：下载Miniconda
**下载地址：** https://docs.conda.io/en/latest/miniconda.html

**文件大小：** 约50MB（比Anaconda小）

#### 步骤2：安装Miniconda
1. 双击运行安装程序
2. 按照向导完成安装
3. 重启终端

#### 步骤3：创建虚拟环境
```bash
# 创建Python 3.11环境
conda create -n medical-ai python=3.11

# 激活环境
conda activate medical-ai
```

#### 步骤4：安装依赖并启动
```bash
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\ai-service
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
python main.py
```

---

## 四、快速对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| Python 3.11安装 | 简单直接，系统级安装 | 可能影响其他项目 | ⭐⭐⭐⭐ |
| Anaconda | 完整环境，隔离性好 | 安装包大（500MB） | ⭐⭐⭐⭐⭐ |
| Miniconda | 轻量级（50MB），灵活 | 需要手动配置更多 | ⭐⭐⭐⭐ |

---

## 五、安装后验证

### 5.1 验证Python版本
```bash
# 检查已安装版本
py -0

# 或使用conda
conda --version
python --version
```

### 5.2 验证依赖安装
```bash
# 检查关键包
python -c "import torch; print(f'PyTorch: {torch.__version__}')"
python -c "import transformers; print(f'Transformers: {transformers.__version__}')"
python -c "import sentence_transformers; print('SentenceTransformers: OK')"
```

### 5.3 启动AI服务
```bash
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\ai-service
python main.py

# 或使用py命令
py -3.11 main.py
```

---

## 六、常见问题

### 6.1 安装后找不到Python 3.11
**解决方案：**
```bash
# 方法1：重启终端或命令提示符
# 方法2：使用py命令
py -3.11 --version

# 方法3：添加到PATH环境变量
# 右键"此电脑" -> 属性 -> 高级系统设置 -> 环境变量
# 在系统变量的Path中添加：
# C:\Users\你的用户名\AppData\Local\Programs\Python\Python311\
```

### 6.2 pip安装速度慢
**解决方案：** 使用国内镜像
```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 或配置全局镜像
pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
```

### 6.3 PyTorch安装问题
**解决方案：**
```bash
# 安装CPU版本（推荐）
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

# 或安装GPU版本（需要CUDA）
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
```

---

## 七、推荐安装顺序

### 7.1 最快路径（推荐）
1. ✅ 安装Anaconda（约500MB，但环境最完整）
2. ✅ 创建Python 3.11虚拟环境
3. ✅ 安装依赖包
4. ✅ 启动AI服务

### 7.2 最简单路径
1. ✅ 下载Python 3.11安装包
2. ✅ 安装时勾选"Add Python to PATH"
3. ✅ 使用py -3.11安装依赖
4. ✅ 启动AI服务

---

## 八、后续测试流程

### 8.1 安装完成后
```bash
# 1. 验证Python版本
python --version  # 应显示 Python 3.11.x

# 2. 安装依赖
cd d:\IdeaProjects\2026年人工智能工程实训\结业实训\ai-service
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 3. 启动AI服务
python main.py

# 4. 测试接口（新开终端）
curl http://localhost:8000/api/ai/health
```

### 8.2 集成测试
```bash
# 1. 启动后端服务
cd ..\backend
mvn spring-boot:run

# 2. 测试完整流程
curl -X POST http://localhost:8080/api/visits -d '{...}'
curl -X POST http://localhost:8080/api/visits/1/analyze
```

---

## 九、下载链接汇总

| 软件 | 下载地址 | 文件大小 |
|------|---------|---------|
| Python 3.11.9 | https://www.python.org/downloads/release/python-3119/ | ~25MB |
| Anaconda | https://www.anaconda.com/download | ~500MB |
| Miniconda | https://docs.conda.io/en/latest/miniconda.html | ~50MB |

---

## 十、总结

**当前状态：** 
- ✅ Python 3.14.0已安装
- ❌ Python 3.9/3.10未安装
- ❌ AI服务无法启动（兼容性问题）

**推荐方案：** 
- 🥇 **Anaconda + Python 3.11虚拟环境**（最推荐）
- 🥈 **直接安装Python 3.11**（简单快捷）
- 🥉 **Miniconda + Python 3.11虚拟环境**（轻量级）

**下一步行动：**
1. 选择一种安装方案
2. 下载并安装Python 3.11或Anaconda
3. 创建虚拟环境（如果使用Anaconda）
4. 安装依赖包
5. 启动AI服务进行测试

**备注：** 
- 建议优先使用Anaconda方案，因为它提供了完整的数据科学环境
- Python 3.11是目前深度学习生态系统支持最稳定的版本
- 安装完成后请重新打开终端以刷新环境变量