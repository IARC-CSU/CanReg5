# build_r_bundle.ps1
# Automates downloading, configuring, and packaging a portable R runtime for CanReg5 on Windows.
param (
    [string]$RVersion = "4.3.3",
    [string]$OutputDir = "r-runtime"
)

$ErrorActionPreference = "Stop"

Write-Host "=== Building Portable R Bundle for CanReg5 (R $RVersion) ==="

$InstallerUrl = "https://cran.r-project.org/bin/windows/base/old/R-$RVersion-win.exe"
$InstallerPath = "$env:TEMP\R-$RVersion-win.exe"
$TempInstallDir = "$env:TEMP\R-portable-$RVersion"

Write-Host "Downloading R $RVersion from $InstallerUrl..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-WebRequest -Uri $InstallerUrl -OutFile $InstallerPath

Write-Host "Installing silently to $TempInstallDir..."
if (Test-Path $TempInstallDir) {
    Remove-Item -Recurse -Force $TempInstallDir
}
Start-Process -FilePath $InstallerPath -ArgumentList "/VERYSILENT /DIR=`"$TempInstallDir`" /COMPONENTS=`"main,x64files`"" -Wait

Write-Host "Installing required CanReg5 packages..."
$RScriptExe = "$TempInstallDir\bin\Rscript.exe"

$Packages = @(
    "Rcpp", "data.table", "ggplot2", "shiny", "shinydashboard", "shinyjs",
    "gridExtra", "scales", "Cairo", "officer", "flextable", "zip",
    "bmp", "jpeg", "png", "shiny.i18n", "XML", "plyr", "stringr",
    "dplyr", "RJSONIO", "jsonlite", "lubridate", "reshape2", "tidyr",
    "gtools", "Rcan"
)
$PkgString = ($Packages | ForEach-Object { "`"$_`"" }) -join ", "

$InstallCmd = "install.packages(c($PkgString), repos='https://cran.r-project.org', type='win.binary', quiet=TRUE)"
Start-Process -FilePath $RScriptExe -ArgumentList "-e `"$InstallCmd`"" -Wait

Write-Host "Cleaning up unneeded files to minimize bundle size..."
$Unneeded = @(
    "$TempInstallDir\doc\manual",
    "$TempInstallDir\tests"
)
foreach ($item in $Unneeded) {
    if (Test-Path $item) {
        Remove-Item -Recurse -Force $item
    }
}

Write-Host "Configuring Rprofile.site for portable execution..."
$ProfilePath = "$TempInstallDir\etc\Rprofile.site"
Add-Content -Path $ProfilePath -Value "`n# CanReg5 Portable R configuration`noptions(repos = c(CRAN = 'https://cran.r-project.org'))`noptions(pkgType = 'win.binary')`n"

Write-Host "Copying to target directory: $OutputDir..."
if (Test-Path $OutputDir) {
    Remove-Item -Recurse -Force $OutputDir
}
Copy-Item -Recurse -Force $TempInstallDir $OutputDir

Write-Host "=== Portable R Bundle successfully created in $OutputDir ==="

