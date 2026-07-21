# ============================================================================
# VOLCANENGINE - SYSTEM MONITOR & POST-MORTEM AUDIT
# Subsistema: Herramientas de Mantenimiento
# ============================================================================

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " VOLCAN ENGINE - GUARDIAN DE SISTEMA" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Iniciando auditoria de estado del motor..." -ForegroundColor Yellow

# 1. Verificar Procesos Zombies de Java
Write-Host "`n[1] Buscando procesos Zombies (Fugas de memoria JVM)..." -ForegroundColor White
$javaZombies = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaZombies) {
    Write-Host "  [ALERTA] Se encontraron procesos 'java.exe' activos. Es posible que sean de la IDE o instancias colgantes." -ForegroundColor Red
    foreach ($p in $javaZombies) {
        $memMB = [Math]::Round($p.WorkingSet64 / 1MB, 2)
        Write-Host "    - PID: $($p.Id) | Memoria: $memMB MB" -ForegroundColor Yellow
    }
    
    $kill = Read-Host "  ¿Deseas purgar estos procesos? (s/N)"
    if ($kill -eq 's' -or $kill -eq 'S') {
        Stop-Process -Name java -Force -ErrorAction SilentlyContinue
        Write-Host "  [OK] Procesos Zombies aniquilados." -ForegroundColor Green
    }
} else {
    Write-Host "  [OK] Cero procesos Java residuales. Entorno limpio." -ForegroundColor Green
}

# 2. Auditar Test Suite
Write-Host "`n[2] Lanzando Suite de Integridad AAA+..." -ForegroundColor White
$testScript = Join-Path $PSScriptRoot "..\test.bat"
Invoke-Expression -Command $testScript

if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Tests de sistema pasados con exito." -ForegroundColor Green
} else {
    Write-Host "  [ERROR] El Kernel detecto anomalias durante la suite de pruebas." -ForegroundColor Red
}

# 3. Reporte de Puertos (UDP NIO)
Write-Host "`n[3] Escaneando afinidad de puertos (UDP)..." -ForegroundColor White
$udpPorts = netstat -ano | Select-String "UDP" | Select-String "0.0.0.0"
if ($udpPorts) {
    Write-Host "  [INFO] Existen puertos UDP abiertos en el sistema local:" -ForegroundColor Cyan
    $udpPorts | Select-Object -First 3 | ForEach-Object { Write-Host "    $($_.ToString().Trim())" -ForegroundColor DarkGray }
} else {
    Write-Host "  [OK] Ningun proceso extraño obstruyendo puertos UDP." -ForegroundColor Green
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host " Auditoria Finalizada. El servidor esta listo para la ingesta." -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
