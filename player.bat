@echo off
setlocal
pushd "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\rotate-log.ps1"
java -cp "bin/class;./presents;./bin;./bin/*" -Djava.library.path=./bin/lib/win64 dnd.PlanetariumPlayer %*
set "PLAYER_EXIT=%ERRORLEVEL%"
popd
exit /b %PLAYER_EXIT%
