# Smart Porter Startup Script for PowerShell
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  Starting Smart Porter Server (Rapido/Uber for Railway Stations)" -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Compiling Java source files..." -ForegroundColor Gray
$javaFiles = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -d bin -cp "bin;lib/*" $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed. Ensure JDK is in your PATH." -ForegroundColor Red
    Exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Server starting at: http://localhost:8080/SmartPorter/" -ForegroundColor Green
Write-Host "Alternative root URL: http://localhost:8080/" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the server." -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

java -cp "bin;lib/*" com.smartporter.ServerRunner
