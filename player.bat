@echo off
setlocal
pushd "%~dp0"

set "PLAYER_JAVA=java"
if exist ".\runtime\bin\java.exe" set "PLAYER_JAVA=.\runtime\bin\java.exe"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\check-runtime.ps1" -JavaExecutable "%PLAYER_JAVA%"
if errorlevel 1 goto runtime_error

powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\rotate-log.ps1"
"%PLAYER_JAVA%" -cp "bin/class;./presents;./bin;./bin/*" -Djava.library.path=./bin/lib/win64 dnd.PlanetariumPlayer %*
set "PLAYER_EXIT=%ERRORLEVEL%"
popd
exit /b %PLAYER_EXIT%

:runtime_error
set "PLAYER_EXIT=%ERRORLEVEL%"
echo.
echo Player was not started: Java or JCEF runtime is missing or damaged.
pause
popd
exit /b %PLAYER_EXIT%
