# 推送代码到 GitHub
# 网络恢复后运行此脚本

Write-Host "正在推送代码到 GitHub..." -ForegroundColor Cyan

# 检查是否有未推送的提交
$needPush = git status --porcelain=v2 --branch | Select-String "ahead"
if (-not $needPush) {
    Write-Host "没有需要推送的提交" -ForegroundColor Green
    exit 0
}

# 尝试推送（最多重试 10 次）
$maxRetry = 10
for ($i = 1; $i -le $maxRetry; $i++) {
    Write-Host "尝试 $i/$maxRetry..." -ForegroundColor Yellow
    
    git push origin backend-dev
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n推送成功！" -ForegroundColor Green
        exit 0
    }
    
    if ($i -lt $maxRetry) {
        Write-Host "推送失败，等待 5 秒后重试..." -ForegroundColor Red
        Start-Sleep -Seconds 5
    }
}

Write-Host "`n推送失败，请检查网络连接" -ForegroundColor Red
Write-Host "可能的原因：" -ForegroundColor Yellow
Write-Host "1. 网络无法访问 GitHub" -ForegroundColor Yellow
Write-Host "2. 需要使用代理/VPN" -ForegroundColor Yellow
Write-Host "3. DNS 解析问题" -ForegroundColor Yellow

# 提供替代方案
Write-Host "`n替代方案：" -ForegroundColor Cyan
Write-Host "1. 开启加速器/VPN 后重试" -ForegroundColor Cyan
Write-Host "2. 修改 DNS 为 8.8.8.8 或 114.114.114.114" -ForegroundColor Cyan
Write-Host "3. 使用手机热点网络重试" -ForegroundColor Cyan