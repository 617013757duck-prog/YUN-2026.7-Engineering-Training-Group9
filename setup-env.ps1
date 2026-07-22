# 后续操作脚本
# 请在网络恢复后运行此脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  医疗平台后端环境配置脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. 配置环境变量（JDK 已安装）
Write-Host "`n[1/4] 配置 JDK 环境变量..." -ForegroundColor Yellow
$jdkPath = "C:\Program Files\AdoptOpenJDK\jdk-17.0.0.20-hotspot"
if (Test-Path $jdkPath) {
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkPath, "User")
    $path = [Environment]::GetEnvironmentVariable("PATH", "User")
    if (-not $path.Contains("jdk-17")) {
        [Environment]::SetEnvironmentVariable("PATH", "$path;$jdkPath\bin", "User")
    }
    Write-Host "JDK 环境变量配置完成" -ForegroundColor Green
} else {
    Write-Host "JDK 未安装，请先安装 JDK 17" -ForegroundColor Red
}

# 2. 下载 Maven
Write-Host "`n[2/4] 下载 Maven..." -ForegroundColor Yellow
$mavenVersion = "3.9.6"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$mavenFile = "C:\Tools\apache-maven.zip"

try {
    if (-not (Test-Path "C:\Tools\apache-maven-$mavenVersion")) {
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenFile -TimeoutSec 300
        Expand-Archive -Path $mavenFile -DestinationPath "C:\Tools" -Force
        Remove-Item $mavenFile
        Write-Host "Maven 下载完成" -ForegroundColor Green
    } else {
        Write-Host "Maven 已存在，跳过下载" -ForegroundColor Green
    }
} catch {
    Write-Host "Maven 下载失败，请手动下载: $mavenUrl" -ForegroundColor Red
}

# 配置 Maven 环境变量
$mavenHome = "C:\Tools\apache-maven-$mavenVersion"
if (Test-Path $mavenHome) {
    [Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, "User")
    $path = [Environment]::GetEnvironmentVariable("PATH", "User")
    if (-not $path.Contains("apache-maven")) {
        [Environment]::SetEnvironmentVariable("PATH", "$path;$mavenHome\bin", "User")
    }
    Write-Host "Maven 环境变量配置完成" -ForegroundColor Green
}

# 3. 初始化数据库
Write-Host "`n[3/4] 初始化数据库..." -ForegroundColor Yellow
Write-Host "请在 MySQL 客户端或 Navicat 中执行 database/init.sql 文件" -ForegroundColor Cyan

# 4. 推送代码到 GitHub
Write-Host "`n[4/4] 推送代码到 GitHub..." -ForegroundColor Yellow
Write-Host "执行: git push origin backend-dev" -ForegroundColor Cyan

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  配置完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n请重启终端使环境变量生效" -ForegroundColor Yellow
Write-Host "然后运行: mvn -version 和 java -version 验证安装" -ForegroundColor Yellow