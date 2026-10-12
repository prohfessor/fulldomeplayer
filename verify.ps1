$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "cdnd.bat")
if ($LASTEXITCODE -ne 0) { throw "Java compilation failed" }

& java -cp (Join-Path $PSScriptRoot "bin\class") dnd.PlaylistCodecTest
if ($LASTEXITCODE -ne 0) { throw "Java tests failed" }

$webRoot = "E:\www\player"
& node --check (Join-Path $webRoot "libs\FullDomeRenderer.js")
if ($LASTEXITCODE -ne 0) { throw "Renderer syntax check failed" }
& node --check (Join-Path $webRoot "libs\FullDomeRendererPlayer.js")
if ($LASTEXITCODE -ne 0) { throw "Web player bootstrap syntax check failed" }

& node --check (Join-Path $webRoot "libs\common.js")
if ($LASTEXITCODE -ne 0) { throw "Common browser utilities syntax check failed" }
& node --check (Join-Path $webRoot "scenes\cubes.js")
if ($LASTEXITCODE -ne 0) { throw "Scene syntax check failed" }

Write-Output "Verification: OK"
