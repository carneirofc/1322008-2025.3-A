param(
    [ValidateSet("clean", "compile", "jar")]
    [string]$Task = "jar"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BuildScript = Join-Path $ProjectRoot "build.ps1"

if (!(Test-Path $BuildScript)) {
    throw "Build script nao encontrado em '$BuildScript'."
}

& $BuildScript -Task $Task
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if ($Task -eq "jar") {
    Write-Host "Build concluido: target/calculator.jar"
}
