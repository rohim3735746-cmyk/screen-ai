@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

:execute
if exist "%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" (
    "%JAVA_EXE%" -jar "%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" %*
) else (
    gradle %*
)
goto end

:fail
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
exit /b 1

:end
