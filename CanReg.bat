@echo off
rem CanReg5 Windows Batch Launcher Wrapper
setlocal
cd /d "%~dp0"

REM Find java executable (prefer bundled jlink runtime if present)
set JAVACMD=
if exist "%~dp0jre\bin\javaw.exe" (
    set "JAVACMD=%~dp0jre\bin\javaw.exe"
) else if exist "%~dp0dist\jre\bin\javaw.exe" (
    set "JAVACMD=%~dp0dist\jre\bin\javaw.exe"
) else (
    where javaw.exe >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        set JAVACMD=javaw.exe
    ) else if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javaw.exe" (
            set "JAVACMD=%JAVA_HOME%\bin\javaw.exe"
        )
    )
)
if "%JAVACMD%"=="" set JAVACMD=javaw.exe

if exist "CanReg.jar" (
    start "" "%JAVACMD%" -Xms256m -Xmx1024m -cp "CanReg.jar;lib/*" canreg.client.CanRegClientApp %*
) else if exist "dist\CanReg.jar" (
    start "" "%JAVACMD%" -Xms256m -Xmx1024m -cp "dist\CanReg.jar;dist\lib/*" canreg.client.CanRegClientApp %*
) else (
    echo Error: CanReg.jar could not be found.
    pause
)
endlocal

