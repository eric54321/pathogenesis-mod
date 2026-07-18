# deploy.ps1 — Build Pathogenesis, version it, and copy the JAR to your
# Fabric mods folder AND the dist/ folder for distribution to friends.
# Run this from the project root: .\deploy.ps1

$modsFolder = "$env:APPDATA\.minecraft-pathogenesis\mods"
$distFolder = "dist"

# Version scheme: YYYY.MM.DD-N, where N auto-increments for multiple builds in one day.
$today = Get-Date -Format "yyyy.MM.dd"
$existingToday = Get-ChildItem "$distFolder\pathogenesis-$today-*.jar" -ErrorAction SilentlyContinue
$buildNumber = 1
if ($existingToday) {
    $buildNumber = ($existingToday | ForEach-Object {
        if ($_.Name -match "-(\d+)\.jar$") { [int]$matches[1] } else { 0 }
    } | Measure-Object -Maximum).Maximum + 1
}
$version = "$today-$buildNumber"

Write-Host "Building Pathogenesis v$version..." -ForegroundColor Cyan
# Pass args as an array (not a single string) so PowerShell doesn't mis-tokenize
# the "-P" flag the way it does with an inline "--%" or plain string argument.
& .\gradlew.bat @("build", "-Pmod_version=$version")

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed. Fix the errors above and try again." -ForegroundColor Red
    exit 1
}

# Find the JAR we just built, matched by the exact version string — build\libs\
# accumulates jars from every past build, so matching by name (not just "newest"
# or "first") guarantees we grab the one that matches $version, not a stale one.
$jar = Get-ChildItem "build\libs\pathogenesis-$version.jar" -ErrorAction SilentlyContinue

if (-not $jar) {
    Write-Host "Could not find built JAR build\libs\pathogenesis-$version.jar" -ForegroundColor Red
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

# Copy the new JAR to the mods folder
Copy-Item $jar.FullName $modsFolder
Write-Host "Deployed $($jar.Name) to $modsFolder" -ForegroundColor Green

# Copy the versioned JAR into dist/ (kept as build history) and refresh the
# stable "latest" copy friends should always grab.
if (-not (Test-Path $distFolder)) { New-Item -ItemType Directory -Path $distFolder | Out-Null }
Copy-Item $jar.FullName "$distFolder\$($jar.Name)"
Copy-Item $jar.FullName "$distFolder\pathogenesis-latest.jar"
Write-Host "Saved $($jar.Name) and updated pathogenesis-latest.jar in $distFolder\" -ForegroundColor Green

Write-Host "Start Minecraft with the Fabric 1.21.1 profile to test." -ForegroundColor Green
