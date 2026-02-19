param(
    [switch]$Rebuild,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$AppArgs
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BuildScript = Join-Path $ProjectRoot "build.ps1"
$JarPath = Join-Path $ProjectRoot "target\calculator.jar"

if (!(Test-Path $BuildScript)) {
    throw "Build script nao encontrado em '$BuildScript'."
}

if ($Rebuild) {
    & $BuildScript -Task jar
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} elseif (!(Test-Path $JarPath)) {
    & $BuildScript -Task jar
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

& $BuildScript -Task run -AppArgs $AppArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
