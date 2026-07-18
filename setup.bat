@echo off
setlocal enabledelayedexpansion

echo.
echo ===================================
echo   Plague of Danjin - Setup
echo ===================================
echo.

:: --- Check Java ---
set JAVA_OK=0
set JAVA_VER=0

java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH.
    goto :install_java
)

:: Parse Java version
for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER_STR=%%~a
)

:: Extract major version number
for /f "delims=." %%a in ("%JAVA_VER_STR%") do (
    set JAVA_VER=%%a
)

:: Handle versions like "1.8.x" (Java 8 style)
if "%JAVA_VER%"=="1" (
    for /f "tokens=2 delims=." %%a in ("%JAVA_VER_STR%") do (
        set JAVA_VER=%%a
    )
)

if %JAVA_VER% GEQ 17 (
    echo [OK] Java %JAVA_VER% detected. OK!
    set JAVA_OK=1
    goto :check_java_home
) else (
    echo [WARN] Java %JAVA_VER% detected, but Java 17+ is required.
    goto :install_java
)

:install_java
echo.

:: Check for winget
winget --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Windows Package Manager (winget) detected.
    echo [INFO] Can install Java 17 via: winget install Microsoft.OpenJDK.17
    echo.
    set /p INSTALL_CHOICE="Would you like to install Java 17 now? (y/n) "
    if /i "!INSTALL_CHOICE!"=="y" (
        echo [INSTALL] Running: winget install Microsoft.OpenJDK.17
        winget install Microsoft.OpenJDK.17
        echo.
        echo [INFO] Please restart this script after installation completes.
        echo [INFO] You may need to open a new terminal for PATH changes to take effect.
        pause
        exit /b 0
    ) else (
        goto :manual_install
    )
)

:: Check for Chocolatey
choco --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Chocolatey detected.
    echo [INFO] Can install Java 17 via: choco install openjdk17
    echo.
    set /p INSTALL_CHOICE="Would you like to install Java 17 now? (y/n) "
    if /i "!INSTALL_CHOICE!"=="y" (
        echo [INSTALL] Running: choco install openjdk17 -y
        choco install openjdk17 -y
        echo.
        echo [INFO] Please restart this script after installation completes.
        echo [INFO] You may need to open a new terminal for PATH changes to take effect.
        pause
        exit /b 0
    ) else (
        goto :manual_install
    )
)

:manual_install
echo.
echo [INFO] No supported package manager found (winget or choco).
echo [INFO] Download Java 17 manually from: https://adoptium.net/temurin/releases/
echo.
pause
exit /b 1

:check_java_home
echo.

:: Check JAVA_HOME
if defined JAVA_HOME (
    echo [OK] JAVA_HOME is set to: %JAVA_HOME%
) else (
    echo [WARN] JAVA_HOME is not set.
    echo [HINT] Set it via: System Properties ^> Environment Variables ^> New
    echo         Variable: JAVA_HOME
    echo         Value: C:\Program Files\Eclipse Adoptium\jdk-17.x.x (or your JDK path)
)

:: --- Verification build ---
echo.
echo [BUILD] Running verification build: gradlew.bat classes
echo.

call gradlew.bat classes
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================
    echo   Setup complete!
    echo ===================================
    echo.
    echo   Run the game with: gradlew.bat run
    echo.
) else (
    echo.
    echo ===================================
    echo   Build failed!
    echo ===================================
    echo.
    echo [ERROR] The build did not succeed.
    echo   Please check that Java 17+ is correctly installed and JAVA_HOME is set.
    echo.
)

pause
exit /b 0
