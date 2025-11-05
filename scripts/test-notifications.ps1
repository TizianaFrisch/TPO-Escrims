# PowerShell script to register a user, create scrim and check notifications
$baseUrl = "http://localhost:8080"
$ts = [int][double]::Parse((Get-Date -UFormat %s))
$userEmail = "user_test_${ts}@example.com"
$password = "Secret123!"

Write-Host "Registering user: $userEmail"
$regBody = @{ email = $userEmail; password = $password; region = "LATAM"; notifyPush = $true; notifyEmail = $true; notifyDiscord = $true } | ConvertTo-Json
try{
    $r = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/register" -Body $regBody -ContentType "application/json" -ErrorAction Stop
    $userId = $r.id
    Write-Host "Registered user id: $userId"
} catch {
    Write-Host "Register failed: $_"
    exit 1
}

Write-Host "Verifying user"
Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/verify/$userId"

Write-Host "Logging in"
$loginBody = @{ email = $userEmail; password = $password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -Body $loginBody -ContentType "application/json"
if ($login -eq $null) { Write-Host "Login failed"; exit 1 }
Write-Host "Login OK"

# Create scrim
Write-Host "Creating scrim"
$scrimBody = @{
    juegoId = 1;
    region = "LATAM";
    formato = "5v5";
    rangoMin = 0;
    rangoMax = 9999;
    latenciaMax = 80;
    fechaHora = "2025-10-18T21:00:00";
    cuposTotal = 2;
    duracionMinutos = 60
} | ConvertTo-Json
$scrim = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/scrims" -Body $scrimBody -ContentType "application/json"
$scrimId = $scrim.id
Write-Host "Scrim created id: $scrimId"

Start-Sleep -Milliseconds 500

Write-Host "Checking notifications for $userEmail"
$notifs = Invoke-RestMethod -Method Get -Uri "$baseUrl/api/notificaciones/usuario/$userEmail"
Write-Host "Notifications count: " $notifs.Length
Write-Host (ConvertTo-Json $notifs -Depth 5)

if ($notifs.Length -gt 0) {
    Write-Host "✅ Notifications found"
    exit 0
} else {
    Write-Host "❌ No notifications"
    exit 2
}