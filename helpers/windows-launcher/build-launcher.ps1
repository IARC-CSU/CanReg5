# Builds CanReg.exe (modern 64-bit Windows GUI launcher) from CanRegLauncher.cs using .NET Framework csc.exe.
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$SourceFile = Join-Path $PSScriptRoot "CanRegLauncher.cs"
$IconFile = Join-Path $RepoRoot "src\canreg\client\gui\resources\LogoBetaNew64x64.ico"
$OutputExe = Join-Path $RepoRoot "CanReg.exe"

if (-not (Test-Path $SourceFile)) {
    throw "Launcher source not found: $SourceFile"
}
if (-not (Test-Path $IconFile)) {
    throw "Application icon not found: $IconFile"
}

$CscCandidates = @(
    (Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"),
    (Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe")
)

$CscExe = $CscCandidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (-not $CscExe) {
    $Cmd = Get-Command csc.exe -ErrorAction SilentlyContinue
    if ($Cmd) {
        $CscExe = $Cmd.Source
    }
}

if (-not $CscExe) {
    throw "Could not locate C# compiler (csc.exe) in .NET Framework or PATH."
}

Write-Host "Compiling $OutputExe using $CscExe..."
$TempExe = Join-Path $RepoRoot "CanReg.exe.tmp"
if (Test-Path $TempExe) {
    Remove-Item $TempExe -Force
}

& $CscExe `
    /nologo `
    /target:winexe `
    /optimize+ `
    /platform:anycpu `
    "/win32icon:$IconFile" `
    /r:System.Windows.Forms.dll `
    "/out:$TempExe" `
    "$SourceFile"

if ($LASTEXITCODE -ne 0 -or -not (Test-Path $TempExe)) {
    throw "Failed to compile CanRegLauncher.cs (exit code: $LASTEXITCODE)"
}

Move-Item -Path $TempExe -Destination $OutputExe -Force

$Item = Get-Item $OutputExe
if (-not $Item -or $Item.Length -le 0) {
    throw "Launcher output file missing or empty: $OutputExe"
}

Write-Host ("Built {0} ({1} bytes, updated {2})" -f $Item.FullName, $Item.Length, $Item.LastWriteTime)
