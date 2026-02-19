param(
    [int]$SearchBudget = 60,
    [int]$Seed = 20260219
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$GeneratedTestsDir = Join-Path $ProjectRoot "evosuite\generated-tests"
$GeneratedReportDir = Join-Path $ProjectRoot "evosuite\generated-report"
$ClassesDir = Join-Path $ProjectRoot "target\classes"

if (!(Test-Path $ClassesDir)) {
    Write-Host "Compilando projeto..."
    & (Join-Path $PSScriptRoot "build-app.ps1") -Task compile
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

New-Item -ItemType Directory -Force -Path $GeneratedTestsDir, $GeneratedReportDir | Out-Null

Write-Host "Gerando testes com EvoSuite via Docker (evosuite/evosuite:1.2.0-java-8)..."
docker run --rm `
    -v "${ProjectRoot}:/evosuite" `
    evosuite/evosuite:1.2.0-java-8 `
    -prefix com.example.calculator `
    -projectCP /evosuite/target/classes `
    -criterion BRANCH `
    -seed $Seed `
    "-Dsearch_budget=$SearchBudget" `
    "-Dtest_dir=/evosuite/evosuite/generated-tests" `
    "-Dreport_dir=/evosuite/evosuite/generated-report" `
    "-Dshow_progress=false"

if ($LASTEXITCODE -ne 0) {
    throw "Falha no EvoSuite."
}

$count = (Get-ChildItem -Recurse -File -Path $GeneratedTestsDir -Filter "*_ESTest.java" -ErrorAction SilentlyContinue).Count
Write-Host "`nConcluido. Testes gerados: $count"
Write-Host "Testes   : $GeneratedTestsDir"
Write-Host "Relatorio: $GeneratedReportDir"
