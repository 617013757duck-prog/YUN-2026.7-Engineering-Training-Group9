# AI Service Full Test Script

Write-Host "===== AI Service API Test =====" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $response1 = Invoke-WebRequest -Uri "http://localhost:8000/api/ai/health" -Method GET -UseBasicParsing
    Write-Host "Status: $($response1.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response1.Content)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 2: AI Analyze
Write-Host "Test 2: AI Analyze" -ForegroundColor Yellow
try {
    $body = @{
        visit_id = 1
        symptoms = @("headache", "fever")
        patient_age = 35
        duration_days = 3
    } | ConvertTo-Json

    $response2 = Invoke-WebRequest -Uri "http://localhost:8000/api/ai/analyze" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
    Write-Host "Status: $($response2.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response2.Content)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 3: RAG Retrieve
Write-Host "Test 3: RAG Retrieve" -ForegroundColor Yellow
try {
    $body3 = @{
        query = "headache and fever"
        visit_id = 1
        top_k = 5
    } | ConvertTo-Json

    $response3 = Invoke-WebRequest -Uri "http://localhost:8000/api/ai/retrieve" -Method POST -Body $body3 -ContentType "application/json" -UseBasicParsing
    Write-Host "Status: $($response3.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response3.Content)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "Note: RAG retrieve may need model download" -ForegroundColor Yellow
    Write-Host ""
}

# Test 4: LLM
Write-Host "Test 4: LLM" -ForegroundColor Yellow
try {
    $body4 = @{
        prompt = "Hello"
    } | ConvertTo-Json

    $response4 = Invoke-WebRequest -Uri "http://localhost:8000/api/ai/llm" -Method POST -Body $body4 -ContentType "application/json" -UseBasicParsing
    Write-Host "Status: $($response4.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response4.Content)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host "===== Test Complete =====" -ForegroundColor Cyan