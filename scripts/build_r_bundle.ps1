# build_r_bundle.ps1
# Automates downloading, configuring, and packaging a portable R runtime for CanReg5 on Windows.
param (
    [string]$RVersion = "4.6.2",
    [string]$OutputDir = "r-runtime"
)

$ErrorActionPreference = "Stop"

Write-Host "=== Building Portable R Bundle for CanReg5 (R $RVersion) ==="

$CurrentUrl = "https://cran.r-project.org/bin/windows/base/R-$RVersion-win.exe"
$OldUrl = "https://cran.r-project.org/bin/windows/base/old/$RVersion/R-$RVersion-win.exe"
$ArchiveUrl = "https://cran-archive.r-project.org/bin/windows/base/old/$RVersion/R-$RVersion-win.exe"
$GenericUrl = "https://cran.r-project.org/bin/windows/base/R-win.exe"
$InstallerPath = "$env:TEMP\R-$RVersion-win.exe"
$TempInstallDir = "$env:TEMP\R-portable-$RVersion"

Write-Host "Downloading R $RVersion installer..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$Downloaded = $false
foreach ($Url in @($CurrentUrl, $OldUrl, $ArchiveUrl, $GenericUrl)) {
    try {
        Write-Host "Trying $Url..."
        Invoke-WebRequest -Uri $Url -OutFile $InstallerPath -UseBasicParsing -TimeoutSec 120
        if ((Test-Path $InstallerPath) -and (Get-Item $InstallerPath).Length -gt 10000000) {
            $Downloaded = $true
            Write-Host "Successfully downloaded R installer from $Url."
            break
        }
    } catch {
        Write-Host "Failed downloading from ${Url} - $($_.Exception.Message)"
    }
}

if (-not $Downloaded) {
    throw "Unable to download R $RVersion installer from any CRAN repository."
}

Write-Host "Installing silently to $TempInstallDir..."
if (Test-Path $TempInstallDir) {
    Remove-Item -Recurse -Force $TempInstallDir
}

$InstallArgs = "/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /DIR=`"$TempInstallDir`""
Write-Host "Executing: $InstallerPath $InstallArgs"
$Proc = Start-Process -FilePath $InstallerPath -ArgumentList $InstallArgs -Wait -PassThru -NoNewWindow
if ($Proc.ExitCode -ne 0) {
    throw "R silent installer exited with error code $($Proc.ExitCode)"
}

$RScriptExe = "$TempInstallDir\bin\Rscript.exe"
if (-not (Test-Path $RScriptExe)) {
    if (Test-Path "$TempInstallDir\bin\x64\Rscript.exe") {
        $RScriptExe = "$TempInstallDir\bin\x64\Rscript.exe"
    } else {
        throw "Rscript.exe was not found at $RScriptExe after installation."
    }
}
Write-Host "Using Rscript: $RScriptExe"

Write-Host "Installing required CanReg5 packages..."
$Packages = @(
    "Rcpp", "data.table", "ggplot2", "shiny", "shinydashboard", "shinyjs",
    "gridExtra", "scales", "Cairo", "officer", "flextable", "zip",
    "bmp", "jpeg", "png", "shiny.i18n", "XML", "plyr", "stringr",
    "dplyr", "RJSONIO", "jsonlite", "lubridate", "reshape2", "tidyr",
    "gtools", "Rcan"
)
$PkgString = ($Packages | ForEach-Object { "'$_'" }) -join ", "

$InstallScript = @"
options(repos = c(CRAN = 'https://cran.r-project.org'))
options(pkgType = 'win.binary')
pkgs <- c($PkgString)
for (p in pkgs) {
    if (!requireNamespace(p, quietly = TRUE)) {
        cat(sprintf('Installing package: %s\n', p))
        install.packages(p, dependencies = c('Depends', 'Imports', 'LinkingTo'), quiet = FALSE)
    } else {
        cat(sprintf('Package already available: %s\n', p))
    }
}
"@

& $RScriptExe -e $InstallScript

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
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
Copy-Item -Path "$TempInstallDir\*" -Destination $OutputDir -Recurse -Force

Write-Host "=== Portable R Bundle successfully created in $OutputDir ==="
