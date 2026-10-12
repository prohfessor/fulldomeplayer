$logPath = Join-Path $PSScriptRoot "debug.log"
$archivePath = Join-Path $PSScriptRoot "debug.previous.log"
$maximumBytes = 10MB

if ((Test-Path -LiteralPath $logPath) -and (Get-Item -LiteralPath $logPath).Length -gt $maximumBytes) {
    if (Test-Path -LiteralPath $archivePath) {
        Remove-Item -LiteralPath $archivePath
    }
    Move-Item -LiteralPath $logPath -Destination $archivePath
}
