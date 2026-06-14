# deploy.ps1 — Build Pathogenesis and copy the JAR to your Fabric mods folder.
# Run this from the project root: .\deploy.ps1

$modsFolder = "$env:APPDATA\.minecraft\mods"
$jarPattern = "build\libs\pathogenesis-*.jar"

Write-Host "Building Pathogenesis..." -ForegroundColor Cyan
.\gradlew.bat build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed. Fix the errors above and try again." -ForegroundColor Red
    exit 1
}

# Find the built JAR (excludes the -sources jar)
$jar = Get-ChildItem $jarPattern | Where-Object { $_.Name -notlike "*-sources*" } | Select-Object -First 1

if (-not $jar) {
    Write-Host "Could not find built JAR in build\libs\" -ForegroundColor Red
    exit 1
}

# Make sure the mods folder exists
if (-not (Test-Path $modsFolder)) {
    Write-Host "Mods folder not found at $modsFolder" -ForegroundColor Red
    Write-Host "Have you run Minecraft with Fabric at least once?" -ForegroundColor Yellow
    exit 1
}

# Remove any old version of the mod so we don't have duplicates
Get-ChildItem "$modsFolder\pathogenesis-*.jar" | Remove-Item -Force

# Copy the new JAR
Copy-Item $jar.FullName $modsFolder
Write-Host "Deployed $($jar.Name) to $modsFolder" -ForegroundColor Green
Write-Host "Start Minecraft with the Fabric 1.21.1 profile to test." -ForegroundColor Green
