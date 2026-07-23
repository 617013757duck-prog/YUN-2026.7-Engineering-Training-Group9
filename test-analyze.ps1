$body = @{
    visit_id = 1
    symptoms = @("头痛", "发热")
    patient_age = 35
    duration_days = 3
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8000/api/ai/analyze -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content