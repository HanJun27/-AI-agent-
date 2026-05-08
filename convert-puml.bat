@echo off
chcp 65001 >nul
echo ========================================
echo PlantUML Diagram Converter
echo Convert .puml to SVG and PNG
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java JDK 8 or higher
    pause
    exit /b 1
)

echo [INFO] Java version:
java -version 2>&1 | findstr "version"
echo.

REM Set variables
set PLANTUML_JAR=tools\plantuml.jar
set INPUT_FILE=docs\architecture.puml
set OUTPUT_DIR=docs\screenshots

REM Create tools directory if not exists
if not exist "tools" mkdir tools

REM Download PlantUML JAR if not exists
if not exist "%PLANTUML_JAR%" (
    echo [INFO] Downloading PlantUML JAR...
    echo URL: https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar' -OutFile '%PLANTUML_JAR%' -UseBasicParsing}"
    if errorlevel 1 (
        echo [ERROR] Failed to download PlantUML JAR
        echo Please download manually from: https://github.com/plantuml/plantuml/releases
        pause
        exit /b 1
    )
    echo [SUCCESS] PlantUML JAR downloaded
    echo.
)

REM Check if input file exists
if not exist "%INPUT_FILE%" (
    echo [ERROR] Input file not found: %INPUT_FILE%
    pause
    exit /b 1
)

echo [INFO] Input file: %INPUT_FILE%
echo [INFO] Output directory: %OUTPUT_DIR%
echo.

REM Create output directory if not exists
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Step 1: Convert PUML to SVG
echo [STEP 1] Converting PUML to SVG...
java -jar "%PLANTUML_JAR%" -tsvg "%INPUT_FILE%" -o "%OUTPUT_DIR%"
if errorlevel 1 (
    echo [ERROR] Failed to convert to SVG
    pause
    exit /b 1
)
echo [SUCCESS] SVG generated successfully
echo.

REM Step 2: Convert PUML to PNG (directly)
echo [STEP 2] Converting PUML to PNG...
java -jar "%PLANTUML_JAR%" -tpng "%INPUT_FILE%" -o "%OUTPUT_DIR%"
if errorlevel 1 (
    echo [ERROR] Failed to convert to PNG
    pause
    exit /b 1
)
echo [SUCCESS] PNG generated successfully
echo.

echo ========================================
echo Conversion Complete!
echo ========================================
echo.
echo Generated files:
dir "%OUTPUT_DIR%\*.svg" /b 2>nul
dir "%OUTPUT_DIR%\*.png" /b 2>nul
echo.
echo Tips:
echo - SVG format: Vector graphics, scalable without quality loss
echo - PNG format: Raster graphics, widely supported
echo - For GitHub README, PNG is recommended for better compatibility
echo.
pause
