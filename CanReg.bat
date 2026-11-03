@echo off
rem CanReg5 Windows Batch Launcher Wrapper
setlocal
cd /d "%~dp0"

REM Find java executable
set JAVACMD=javaw.exe
where javaw.exe >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javaw.exe" (
            set "JAVACMD=%JAVA_HOME%\bin\javaw.exe"
        )
    )
)

if exist "CanReg.jar" (
    start "" "%JAVACMD%" -cp "CanReg.jar;lib/*" canreg.client.CanRegClientApp %*
) else if exist "dist\CanReg.jar" (
    start "" "%JAVACMD%" -cp "dist\CanReg.jar;dist\lib/*" canreg.client.CanRegClientApp %*
) else (
    echo Error: CanReg.jar could not be found.
    pause
)
endlocal

