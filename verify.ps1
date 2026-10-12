$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "cdnd.bat")
if ($LASTEXITCODE -ne 0) { throw "Java compilation failed" }

& java -cp (Join-Path $PSScriptRoot "bin\class") dnd.PlaylistCodecTest
if ($LASTEXITCODE -ne 0) { throw "Java tests failed" }

$webRoot = "E:\www\player"
& node --check (Join-Path $webRoot "libs\FullDomeRenderer.js")
if ($LASTEXITCODE -ne 0) { throw "Renderer syntax check failed" }
& node --check (Join-Path $webRoot "libs\FullDomeRendererPlayer.js")
if ($LASTEXITCODE -ne 0) { throw "Compatibility renderer syntax check failed" }

$canonical = (Get-FileHash (Join-Path $webRoot "libs\FullDomeRenderer.js")).Hash
$compatibility = (Get-FileHash (Join-Path $webRoot "libs\FullDomeRendererPlayer.js")).Hash
if ($canonical -ne $compatibility) {
    throw "Renderer copies differ; update FullDomeRendererPlayer.js from FullDomeRenderer.js"
}

Write-Output "Verification: OK"
