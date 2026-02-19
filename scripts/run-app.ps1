param(
    [switch]$Rebuild,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$AppArgs
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BuildAppScript = Join-Path $PSScriptRoot "build-app.ps1"
$Java = "java"

function Invoke-Build {
    & $BuildAppScript -Task jar
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

$JarFile = Get-ChildItem -Path (Join-Path $ProjectRoot "target") -Filter "*.jar" -ErrorAction SilentlyContinue |
    Select-Object -First 1

if ($Rebuild -or !$JarFile) {
    Invoke-Build
    $JarFile = Get-ChildItem -Path (Join-Path $ProjectRoot "target") -Filter "*.jar" -ErrorAction SilentlyContinue |
        Select-Object -First 1
}

if (!$JarFile) {
    throw "Jar nao encontrado em 'target/'. Execute com -Rebuild para compilar."
}

& $Java -jar $JarFile.FullName @AppArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
