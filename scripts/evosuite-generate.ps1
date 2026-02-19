param(
    [int]$SearchBudget = 60,
    [int]$Seed = 20260219,
    [string]$EvoSuiteVersion = "1.2.0",
    [switch]$DownloadIfMissing,
    [switch]$Clean,
    [string[]]$ExcludeClasses = @("com.example.calculator.CalculatorApp")
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$Java8Home = Join-Path $ProjectRoot "vendor/tools/jdk8u482-b08"
$Java = Join-Path $Java8Home "bin/java.exe"
$Javac = Join-Path $Java8Home "bin/javac.exe"
$EnsureScript = Join-Path $PSScriptRoot "evosuite-ensure.ps1"
$EvoSuiteInfo = & $EnsureScript -ProjectRoot $ProjectRoot -EvoSuiteVersion $EvoSuiteVersion -DownloadIfMissing:$DownloadIfMissing
$EvoSuiteJar = $EvoSuiteInfo.EvoSuiteJarPath

$PackageSourceRoot = Join-Path $ProjectRoot "src/main/java/com/example/calculator"
$PackageClassesRoot = Join-Path $ProjectRoot "evosuite/target/classes-java8subset/com/example/calculator"
$ClassesDir = Join-Path $ProjectRoot "evosuite/target/classes-java8subset"
$GeneratedTestsDir = Join-Path $ProjectRoot "evosuite/generated-tests"
$GeneratedReportDir = Join-Path $ProjectRoot "evosuite/generated-report"

if (!(Test-Path $Java) -or !(Test-Path $Javac)) {
    throw "JDK 8 nao encontrado em '$Java8Home'."
}

if (!(Test-Path $EvoSuiteJar)) {
    throw "EvoSuite jar nao encontrado em '$EvoSuiteJar'."
}

if ($Clean) {
    Remove-Item -Recurse -Force $ClassesDir, $GeneratedTestsDir, $GeneratedReportDir -ErrorAction SilentlyContinue
}

New-Item -ItemType Directory -Force -Path $ClassesDir, $GeneratedTestsDir, $GeneratedReportDir | Out-Null

if (!(Test-Path $PackageSourceRoot)) {
    throw "Codigo-fonte nao encontrado em '$PackageSourceRoot'."
}

$SourceFiles = Get-ChildItem -Path $PackageSourceRoot -Recurse -Filter "*.java" -File |
    Select-Object -ExpandProperty FullName

if (!$SourceFiles -or $SourceFiles.Count -eq 0) {
    throw "Nenhum arquivo .java encontrado em '$PackageSourceRoot'."
}

Write-Host "Compilando classes-alvo para Java 8..."
& $Javac -source 1.8 -target 1.8 -d $ClassesDir @SourceFiles
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (!(Test-Path $PackageClassesRoot)) {
    throw "Classes compiladas nao encontradas em '$PackageClassesRoot'."
}

$TargetClasses = Get-ChildItem -Path $PackageClassesRoot -Recurse -Filter "*.class" -File |
    Where-Object { $_.Name -notlike '*$*' } |
    ForEach-Object {
        $relative = $_.FullName.Substring($ClassesDir.Length + 1)
        $className = $relative.Substring(0, $relative.Length - 6).Replace('\', '.')
        $className
    } |
    Sort-Object -Unique

if ($ExcludeClasses -and $ExcludeClasses.Count -gt 0) {
    $TargetClasses = $TargetClasses | Where-Object { $ExcludeClasses -notcontains $_ }
}

if (!$TargetClasses -or $TargetClasses.Count -eq 0) {
    throw "Nenhuma classe-alvo restante para gerar testes apos aplicar exclusoes."
}

Write-Host "Classes alvo ($($TargetClasses.Count)):"
foreach ($class in $TargetClasses) {
    Write-Host " - $class"
}

Write-Host "Gerando testes com EvoSuite v$EvoSuiteVersion (criterio BRANCH)..."
foreach ($class in $TargetClasses) {
    & $Java -jar $EvoSuiteJar `
        -class $class `
        -projectCP $ClassesDir `
        -criterion BRANCH `
        -seed $Seed `
        "-Dsearch_budget=$SearchBudget" `
        "-Dtest_dir=$GeneratedTestsDir" `
        "-Dreport_dir=$GeneratedReportDir" `
        "-Dshow_progress=false"

    if ($LASTEXITCODE -ne 0) {
        throw "Falha no EvoSuite para a classe $class."
    }
}

$generatedFiles = Get-ChildItem -Recurse -File -Path $GeneratedTestsDir -Filter "*_ESTest.java" -ErrorAction SilentlyContinue
$count = if ($generatedFiles) { $generatedFiles.Count } else { 0 }

Write-Host "Concluido. Arquivos de teste gerados: $count"
Write-Host "Diretorio de testes: $GeneratedTestsDir"
Write-Host "Diretorio de relatorios EvoSuite: $GeneratedReportDir"
