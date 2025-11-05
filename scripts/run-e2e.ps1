# Runs the app (local profile, H2) and executes the Postman collection headless with newman.
# Requirements:
# - Node.js and newman installed (npm i -g newman)
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\run-e2e.ps1

param(
    [string]$Profile = "local",
    [string]$Port = "8080",
    [string]$CollectionPath = "postman_collection.complete.json"
)

function Write-Info($msg){ Write-Host "[e2e] $msg" -ForegroundColor Cyan }
function Write-Warn($msg){ Write-Host "[e2e] $msg" -ForegroundColor Yellow }
function Write-Err($msg){ Write-Host "[e2e] $msg" -ForegroundColor Red }

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $CollectionPath)){
  Write-Err "Collection not found: $CollectionPath"
  exit 1
}

# Check newman
$newman = (Get-Command newman -ErrorAction SilentlyContinue)
if (-not $newman){
  Write-Warn "newman is not installed. Install with: npm i -g newman"
  exit 2
}

Write-Info "Starting Spring Boot (profile=$Profile)"
$env:SPRING_PROFILES_ACTIVE = $Profile

# Start app in background
$javaProc = Start-Process -FilePath "./mvnw.cmd" -ArgumentList "spring-boot:run" -NoNewWindow -PassThru

# Wait for port
Write-Info "Waiting for http://localhost:$Port/ to be up..."
$maxWaitSec = 120
$ok = $false
for($i=0; $i -lt $maxWaitSec; $i++){
  try{
    $r = Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$Port/actuator/health" -TimeoutSec 2
    if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){ $ok = $true; break }
  } catch { Start-Sleep -Milliseconds 500 }
}
if (-not $ok){
  Write-Err "Server did not start within $maxWaitSec seconds"
  try { $javaProc | Stop-Process -Force } catch {}
  exit 3
}

Write-Info "Running Postman collection with newman..."
try{
  newman run $CollectionPath --reporters cli,junit --reporter-junit-export "./target/newman-results.xml"
  $exitCode = $LASTEXITCODE
} catch {
  $exitCode = 1
}

Write-Info "Stopping Spring Boot..."
try { $javaProc | Stop-Process -Force } catch {}

if ($exitCode -ne 0){
  Write-Err "E2E failed (newman exit code $exitCode). Check output and ./target/newman-results.xml"
  exit $exitCode
}

Write-Info "E2E passed. Results: ./target/newman-results.xml"
exit 0
