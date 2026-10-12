param(
    [string]$JavaExecutable = "java"
)

$ErrorActionPreference = "Stop"

$manifestPath = Join-Path $PSScriptRoot "runtime-manifest.json"
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    Write-Error "JCEF manifest not found: $manifestPath"
    exit 2
}

try {
    $runtimeManifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
} catch {
    Write-Error "Cannot read JCEF manifest: $($_.Exception.Message)"
    exit 2
}

$missing = @()
foreach ($relativePath in $runtimeManifest.requiredFiles) {
    $nativeRelativePath = $relativePath.Replace('/', [IO.Path]::DirectorySeparatorChar)
    $filePath = Join-Path $PSScriptRoot $nativeRelativePath
    if (-not (Test-Path -LiteralPath $filePath -PathType Leaf)) {
        $missing += $relativePath
    }
}

if ($missing.Count -gt 0) {
    Write-Host "JCEF runtime is incomplete: $($runtimeManifest.name)." -ForegroundColor Red
    Write-Host "Missing files:" -ForegroundColor Red
    $missing | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    Write-Host "Install the full offline package of the same player version." -ForegroundColor Yellow
    exit 3
}

$localeDirectory = Join-Path $PSScriptRoot "bin\lib\win64\locales"
if (-not (Get-ChildItem -LiteralPath $localeDirectory -Filter "*.pak" -File -ErrorAction SilentlyContinue)) {
    Write-Error "JCEF locale directory is empty: $localeDirectory"
    exit 3
}

try {
    $javaProcessInfo = New-Object System.Diagnostics.ProcessStartInfo
    $javaProcessInfo.FileName = $JavaExecutable
    $javaProcessInfo.Arguments = "-version"
    $javaProcessInfo.UseShellExecute = $false
    $javaProcessInfo.RedirectStandardError = $true
    $javaProcessInfo.RedirectStandardOutput = $true
    $javaProcessInfo.CreateNoWindow = $true
    $javaProcess = [System.Diagnostics.Process]::Start($javaProcessInfo)
    $versionOutput = $javaProcess.StandardError.ReadToEnd() + $javaProcess.StandardOutput.ReadToEnd()
    $javaProcess.WaitForExit()
    if ($javaProcess.ExitCode -ne 0) { throw "Java exited with code $($javaProcess.ExitCode)." }
} catch {
    Write-Host "Cannot start Java: $JavaExecutable" -ForegroundColor Red
    Write-Host "Install Java 17+ or use the full offline package." -ForegroundColor Yellow
    exit 4
}

if ($versionOutput -notmatch 'version "(?<version>[0-9]+)(?:\.(?<minor>[0-9]+))?') {
    Write-Error "Cannot detect the Java version: $versionOutput"
    exit 4
}
$javaMajor = [int]$Matches.version
if ($javaMajor -eq 1) { $javaMajor = [int]$Matches.minor }
if ($javaMajor -lt 17) {
    Write-Host "This JCEF requires Java 17+, but Java $javaMajor was found." -ForegroundColor Red
    Write-Host "Java executable: $JavaExecutable" -ForegroundColor Red
    Write-Host "Install Java 17+ or use the full offline package." -ForegroundColor Yellow
    exit 4
}

Write-Output "JCEF runtime: OK ($($runtimeManifest.name))"
Write-Output "Java runtime: OK ($javaMajor)"
