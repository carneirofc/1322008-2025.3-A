param(
    [int]$SearchBudget = 60,
    [int]$Seed = 20260219,
    [string]$EvoSuiteVersion = "1.2.0",
    [switch]$DownloadIfMissing,
    [string]$JdkHome = "",
    [string[]]$ExcludeClasses = @("com.example.calculator.CalculatorApp")
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JdkHome)) {
    $JdkHome = Join-Path (Split-Path -Parent $PSScriptRoot) "vendor/tools/jdk8u482-b08"
}

$GenerateScript = Join-Path $PSScriptRoot "evosuite-generate.ps1"
$CoverageScript = Join-Path $PSScriptRoot "evosuite-coverage.ps1"

& $GenerateScript `
    -SearchBudget $SearchBudget `
    -Seed $Seed `
    -EvoSuiteVersion $EvoSuiteVersion `
    -DownloadIfMissing:$DownloadIfMissing `
    -ExcludeClasses $ExcludeClasses `
    -Clean
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& $CoverageScript -JdkHome $JdkHome -EvoSuiteVersion $EvoSuiteVersion -DownloadIfMissing:$DownloadIfMissing
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Pipeline concluido."
