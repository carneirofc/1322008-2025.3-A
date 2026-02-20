<#
.SYNOPSIS
    Run all LLM-generated unit tests and open the JaCoCo coverage report.

.DESCRIPTION
    Executes `mvn test` from the project root, which compiles the production
    sources and all test sources (including the llm/ directory registered via
    build-helper-maven-plugin), runs every JUnit 5 test, and lets JaCoCo
    produce an HTML coverage report.

.EXAMPLE
    .\llm\run-tests.ps1
    .\llm\run-tests.ps1 -OpenReport
#>

param(
    [switch]$OpenReport
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Resolve project root (one level above the llm/ folder)
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot

try {
    $mvn = Join-Path $projectRoot "vendor\tools\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd"
    if (-not (Test-Path $mvn)) {
        throw "Maven not found at: $mvn"
    }

    Write-Host ""
    Write-Host "=== Running unit tests with Maven ===" -ForegroundColor Cyan
    Write-Host "Project root: $projectRoot"
    Write-Host ""

    & $mvn test
    $exitCode = $LASTEXITCODE

    Write-Host ""
    if ($exitCode -eq 0) {
        Write-Host "=== All tests passed ===" -ForegroundColor Green
    } else {
        Write-Host "=== Build/test failed (exit code $exitCode) ===" -ForegroundColor Red
    }

    $reportPath = Join-Path $projectRoot "target\site\jacoco\index.html"
    if (Test-Path $reportPath) {
        Write-Host ""
        Write-Host "Coverage report: $reportPath" -ForegroundColor Yellow
        if ($OpenReport) {
            Start-Process $reportPath
        } else {
            Write-Host "Run with -OpenReport to open it in your browser." -ForegroundColor DarkGray
        }
    } else {
        Write-Host "Coverage report not found at: $reportPath" -ForegroundColor DarkYellow
    }

    exit $exitCode
} finally {
    Pop-Location
}
