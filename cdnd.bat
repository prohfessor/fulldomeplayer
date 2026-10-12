@echo off
setlocal
pushd "%~dp0"
javac -encoding UTF-8 -cp "./bin;./bin/*" ./dnd/*.java -d bin/class
set "PLAYER_EXIT=%ERRORLEVEL%"
popd
exit /b %PLAYER_EXIT%


