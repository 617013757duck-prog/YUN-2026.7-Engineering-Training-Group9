# 医疗平台环境配置脚本
# 运行此脚本配置完整开发环境

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  医疗平台开发环境配置" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 设置环境变量（当前会话）
Write-Host "`n[1/4] 配置环境变量..." -ForegroundColor Yellow

$env:JAVA_HOME = "C:\Program Files\AdoptOpenJDK\jdk-17.0.0.20-hotspot"
$env:MAVEN_HOME = "C:\Tools\apache-maven-3.9.16"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "MAVEN_HOME = $env:MAVEN_HOME" -ForegroundColor Green

# 验证 Java
Write-Host "`n[2/4] 验证 Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java 版本: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "Java 未找到，请检查安装" -ForegroundColor Red
    exit 1
}

# 验证 Maven
Write-Host "`n[3/4] 验证 Maven..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "Maven 版本: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "Maven 未找到，请检查安装" -ForegroundColor Red
    exit 1
}

# MySQL 提示
Write-Host "`n[4/4] MySQL 数据库..." -ForegroundColor Yellow
Write-Host "请确保 MySQL 服务已启动" -ForegroundColor Cyan
Write-Host "如果使用免安装版，请运行:" -ForegroundColor Cyan
Write-Host "  D:\MySQL\MySQL Server 8.0\bin\mysqld.exe --console" -ForegroundColor White
Write-Host ""
Write-Host "然后在另一个终端执行数据库初始化:" -ForegroundColor Cyan
Write-Host "  Get-Content database\init.sql | D:\MySQL\MySQL' 'Server' '8.0\bin\mysql.exe -u root -p07040528hmy" -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  配置完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n下一步操作:" -ForegroundColor Yellow
Write-Host "1. 启动 MySQL 服务" -ForegroundColor White
Write-Host "2. 初始化数据库: 执行 database/init.sql" -ForegroundColor White
Write-Host "3. 编译项目: mvn clean install" -ForegroundColor White
Write-Host "4. 运行项目: mvn spring-boot:run" -ForegroundColor White