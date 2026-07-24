@echo off
echo ====================================
echo 医疗平台数据库初始化脚本
echo ====================================
echo.

echo 请按照以下步骤操作：
echo.
echo 步骤1: 找到MySQL安装路径
echo 常见路径：
echo - C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
echo - C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe
echo - C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe
echo.
echo 步骤2: 打开新的命令提示符窗口，运行：
echo "C:\Program Files\MySQL\MySQL Server X.X\bin\mysql.exe" -u root -p
echo.
echo 步骤3: 输入MySQL root密码
echo.
echo 步骤4: 在MySQL命令行中运行：
echo source D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend\src\main\resources\schema.sql;
echo.
echo 或者直接复制以下命令（需要修改MySQL路径）：
echo.
echo "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p medical_platform ^< "D:\IdeaProjects\2026年人工智能工程实训\结业实训\backend\src\main\resources\schema.sql"
echo.
echo ====================================
echo 数据库密码: 07040528hmy
echo ====================================
pause