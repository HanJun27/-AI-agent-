
# PlantUML Diagram Converter - PowerShell Script
# Convert .puml files to SVG and PNG formats

param(
    [string]$InputFile = "docs/architecture.puml",
    [string]$OutputDir = "docs/screenshots"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PlantUML Diagram Converter" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "[INFO] Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "[SUCCESS] Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java not found. Please install JDK 8 or higher." -ForegroundColor Red
    exit 1
}

# Set paths
$PlantUMLJar = "tools/plantuml.jar"
$FullInputPath = Join-Path $PSScriptRoot $InputFile
$FullOutputDir = Join-Path $PSScriptRoot $OutputDir

# Create directories
if (!(Test-Path "tools")) {
    New-Item -ItemType Directory -Path "tools" | Out-Null
}

if (!(Test-Path $FullOutputDir)) {
    New-Item -ItemType Directory -Path $FullOutputDir | Out-Null
}

# Download PlantUML JAR if needed
if (!(Test-Path $PlantUMLJar)) {
    Write-Host ""
    Write-Host "[INFO] Downloading PlantUML JAR..." -ForegroundColor Yellow
    Write-Host "URL: https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar"
    
    try {
        Invoke-WebRequest -Uri "https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar" -OutFile $PlantUMLJar -UseBasicParsing
        Write-Host "[SUCCESS] Download complete" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Download failed: $_" -ForegroundColor Red
        Write-Host "Please download manually and place in tools/plantuml.jar" -ForegroundColor Yellow
        exit 1
    }
}

# Check input file
if (!(Test-Path $FullInputPath)) {
    Write-Host ""
    Write-Host "[ERROR] Input file not found: $InputFile" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[INFO] Input: $InputFile" -ForegroundColor Cyan
Write-Host "[INFO] Output: $OutputDir" -ForegroundColor Cyan
Write-Host ""

# Convert to SVG
Write-Host "[STEP 1] Converting to SVG..." -ForegroundColor Yellow
try {
    & java -jar $PlantUMLJar -tsvg $FullInputPath -o $FullOutputDir
    Write-Host "[SUCCESS] SVG generated" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] SVG conversion failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Convert to PNG
Write-Host "[STEP 2] Converting to PNG..." -ForegroundColor Yellow
try {
    & java -jar $PlantUMLJar -tpng $FullInputPath -o $FullOutputDir
    Write-Host "[SUCCESS] PNG generated" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] PNG conversion failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Conversion Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# List generated files
Write-Host "Generated files:" -ForegroundColor Cyan
Get-ChildItem $FullOutputDir -Filter "*.svg" | ForEach-Object {
    Write-Host "  ✓ $($_.Name) ($([math]::Round($_.Length/1KB, 2)) KB)" -ForegroundColor Green
}
Get-ChildItem $FullOutputDir -Filter "*.png" | ForEach-Object {
    Write-Host "  ✓ $($_.Name) ($([math]::Round($_.Length/1KB, 2)) KB)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Tips:" -ForegroundColor Cyan
Write-Host "  • SVG: Vector format, infinite scaling" -ForegroundColor White
Write-Host "  • PNG: Raster format, GitHub compatible" -ForegroundColor White
Write-Host "  • Update README.md to reference these images" -ForegroundColor White
Write-Host ""
