param(
    [string]$JdkHome = "",
    [string]$EvoSuiteVersion = "1.2.0",
    [switch]$DownloadIfMissing,
    [switch]$SkipClean
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($JdkHome)) {
    $JdkHome = Join-Path $ProjectRoot "vendor/tools/jdk8u482-b08"
}
$EnsureScript = Join-Path $PSScriptRoot "evosuite-ensure.ps1"
$null = & $EnsureScript -ProjectRoot $ProjectRoot -EvoSuiteVersion $EvoSuiteVersion -DownloadIfMissing:$DownloadIfMissing
$Maven = Join-Path $ProjectRoot "vendor/tools/apache-maven-3.9.9/bin/mvn.cmd"
$GeneratedTestsRoot = Join-Path $ProjectRoot "evosuite/generated-tests"
$TargetTestRoot = Join-Path $ProjectRoot "src/test/java"
$JacocoCsv = Join-Path $ProjectRoot "target/site/jacoco/jacoco.csv"
$SummaryPath = Join-Path $ProjectRoot "target/coverage-summary.json"

if (!(Test-Path $JdkHome)) {
    throw "JDK nao encontrado em '$JdkHome'."
}

if (!(Test-Path $Maven)) {
    throw "Maven nao encontrado em '$Maven'."
}

if (!(Test-Path $GeneratedTestsRoot)) {
    throw "Testes gerados nao encontrados em '$GeneratedTestsRoot'. Execute scripts/evosuite-generate.ps1 primeiro."
}

$env:JAVA_HOME = $JdkHome
$env:Path = "$JdkHome\bin;$env:Path"

New-Item -ItemType Directory -Force -Path $TargetTestRoot | Out-Null

$generatedFiles = Get-ChildItem -Path $GeneratedTestsRoot -Recurse -Filter "*_ESTest*.java" -File -ErrorAction SilentlyContinue
if (!$generatedFiles -or $generatedFiles.Count -eq 0) {
    throw "Nenhum teste *_ESTest*.java encontrado em '$GeneratedTestsRoot'."
}

Get-ChildItem -Path $TargetTestRoot -Recurse -Filter "*_ESTest*.java" -File -ErrorAction SilentlyContinue |
    Remove-Item -Force

foreach ($generatedFile in $generatedFiles) {
    $relativePath = $generatedFile.FullName.Substring($GeneratedTestsRoot.Length + 1)
    $destinationFile = Join-Path $TargetTestRoot $relativePath
    $destinationDir = Split-Path -Parent $destinationFile
    New-Item -ItemType Directory -Force -Path $destinationDir | Out-Null
    Copy-Item -Path $generatedFile.FullName -Destination $destinationFile -Force
}

if ($SkipClean) {
    & $Maven -q test jacoco:report
} else {
    & $Maven -q clean test jacoco:report
}

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (!(Test-Path $JacocoCsv)) {
    throw "Relatorio JaCoCo nao encontrado em '$JacocoCsv'."
}

$rows = Import-Csv -Path $JacocoCsv
if (!$rows -or $rows.Count -eq 0) {
    throw "JaCoCo CSV vazio."
}

$instructionMissed = ($rows | Measure-Object -Property INSTRUCTION_MISSED -Sum).Sum
$instructionCovered = ($rows | Measure-Object -Property INSTRUCTION_COVERED -Sum).Sum
$branchMissed = ($rows | Measure-Object -Property BRANCH_MISSED -Sum).Sum
$branchCovered = ($rows | Measure-Object -Property BRANCH_COVERED -Sum).Sum
$lineMissed = ($rows | Measure-Object -Property LINE_MISSED -Sum).Sum
$lineCovered = ($rows | Measure-Object -Property LINE_COVERED -Sum).Sum
$methodMissed = ($rows | Measure-Object -Property METHOD_MISSED -Sum).Sum
$methodCovered = ($rows | Measure-Object -Property METHOD_COVERED -Sum).Sum

$instructionTotal = $instructionMissed + $instructionCovered
$branchTotal = $branchMissed + $branchCovered
$lineTotal = $lineMissed + $lineCovered
$methodTotal = $methodMissed + $methodCovered

$instructionPct = if ($instructionTotal -gt 0) { [math]::Round(($instructionCovered / $instructionTotal) * 100, 2) } else { 0 }
$branchPct = if ($branchTotal -gt 0) { [math]::Round(($branchCovered / $branchTotal) * 100, 2) } else { 0 }
$linePct = if ($lineTotal -gt 0) { [math]::Round(($lineCovered / $lineTotal) * 100, 2) } else { 0 }
$methodPct = if ($methodTotal -gt 0) { [math]::Round(($methodCovered / $methodTotal) * 100, 2) } else { 0 }

$summary = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    generatedTestsCopied = $generatedFiles.Count
    instructionCoveragePercent = $instructionPct
    branchCoveragePercent = $branchPct
    lineCoveragePercent = $linePct
    methodCoveragePercent = $methodPct
    instructionCovered = $instructionCovered
    instructionTotal = $instructionTotal
    branchCovered = $branchCovered
    branchTotal = $branchTotal
    lineCovered = $lineCovered
    lineTotal = $lineTotal
    methodCovered = $methodCovered
    methodTotal = $methodTotal
    jacocoCsv = "target/site/jacoco/jacoco.csv"
    jacocoHtml = "target/site/jacoco/index.html"
}

$summary | ConvertTo-Json -Depth 4 | Set-Content -Path $SummaryPath -Encoding ascii

Write-Host "Cobertura geral:"
Write-Host " - Instruction: $instructionPct% ($instructionCovered/$instructionTotal)"
Write-Host " - Branch:      $branchPct% ($branchCovered/$branchTotal)"
Write-Host " - Line:        $linePct% ($lineCovered/$lineTotal)"
Write-Host " - Method:      $methodPct% ($methodCovered/$methodTotal)"
Write-Host "Resumo salvo em: $SummaryPath"
