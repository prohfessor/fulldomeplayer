param(
    [string]$Version = (Get-Date -Format "yyyy.MM.dd"),
    [string]$OutputDirectory = (Join-Path $PSScriptRoot "cache\distributions"),
    [string]$JavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    throw "JAVA_HOME is not set. Specify a JDK/JRE with -JavaHome."
}
$javaExecutable = Join-Path $JavaHome "bin\java.exe"
if (-not (Test-Path -LiteralPath $javaExecutable -PathType Leaf)) {
    throw "bin\java.exe was not found in JavaHome: $JavaHome"
}

# Сборка и тесты должны использовать ту же Java, которая войдёт в поставку.
$env:PATH = (Join-Path $JavaHome "bin") + [IO.Path]::PathSeparator + $env:PATH

& (Join-Path $PSScriptRoot "verify.ps1")
if ($LASTEXITCODE -ne 0) { throw "Project verification failed." }
& (Join-Path $PSScriptRoot "check-runtime.ps1") -JavaExecutable $javaExecutable
if ($LASTEXITCODE -ne 0) { throw "Native JCEF runtime is incomplete." }

$safeVersion = $Version -replace '[^0-9A-Za-z._-]', '_'
$packageName = "fulldomeplayer-$safeVersion-windows-x64"
$stageRoot = Join-Path $OutputDirectory $packageName
$zipPath = Join-Path $OutputDirectory "$packageName.zip"

if (Test-Path -LiteralPath $stageRoot) { Remove-Item -LiteralPath $stageRoot -Recurse -Force }
if (Test-Path -LiteralPath $zipPath) { Remove-Item -LiteralPath $zipPath -Force }
New-Item -ItemType Directory -Path $stageRoot -Force | Out-Null

$rootFiles = @(
    "LICENSE", "README.md", "readme.txt", "player.bat", "player.properties",
    "cdnd.bat", "verify.ps1", "rotate-log.ps1", "check-runtime.ps1",
    "runtime-manifest.json"
)
foreach ($file in $rootFiles) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot $file) -Destination $stageRoot
}

Copy-Item -LiteralPath (Join-Path $PSScriptRoot "dnd") -Destination $stageRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "bin") -Destination $stageRoot -Recurse
Copy-Item -LiteralPath $JavaHome -Destination (Join-Path $stageRoot "runtime") -Recurse

Compress-Archive -LiteralPath $stageRoot -DestinationPath $zipPath -CompressionLevel Optimal
Remove-Item -LiteralPath $stageRoot -Recurse -Force

$hash = Get-FileHash -LiteralPath $zipPath -Algorithm SHA256
Write-Output "Offline package created: $zipPath"
Write-Output "SHA-256: $($hash.Hash)"
