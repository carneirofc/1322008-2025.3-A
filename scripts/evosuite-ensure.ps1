param(
    [string]$ProjectRoot,
    [string]$EvoSuiteVersion = "1.2.0",
    [switch]$DownloadIfMissing
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    $ProjectRoot = Split-Path -Parent $PSScriptRoot
}

$EvoSuiteDir = Join-Path $ProjectRoot "evosuite"
$EvoSuiteJarName = "evosuite-$EvoSuiteVersion.jar"
$RuntimeJarName = "evosuite-standalone-runtime-$EvoSuiteVersion.jar"

$EvoSuiteJarPath = Join-Path $EvoSuiteDir $EvoSuiteJarName
$RuntimeJarPath = Join-Path $EvoSuiteDir $RuntimeJarName

function Get-ReleaseAssetUrl {
    param([string]$AssetName)
    return "https://github.com/EvoSuite/evosuite/releases/download/v$EvoSuiteVersion/$AssetName"
}

if ($DownloadIfMissing) {
    New-Item -ItemType Directory -Force -Path $EvoSuiteDir | Out-Null

    if (!(Test-Path $EvoSuiteJarPath)) {
        $url = Get-ReleaseAssetUrl -AssetName $EvoSuiteJarName
        Write-Host "Baixando $EvoSuiteJarName..."
        Invoke-WebRequest -Uri $url -OutFile $EvoSuiteJarPath
    }

    if (!(Test-Path $RuntimeJarPath)) {
        $url = Get-ReleaseAssetUrl -AssetName $RuntimeJarName
        Write-Host "Baixando $RuntimeJarName..."
        Invoke-WebRequest -Uri $url -OutFile $RuntimeJarPath
    }
}

if (!(Test-Path $EvoSuiteJarPath)) {
    throw "Arquivo nao encontrado: '$EvoSuiteJarPath'. Use -DownloadIfMissing para baixar do release oficial."
}

if (!(Test-Path $RuntimeJarPath)) {
    throw "Arquivo nao encontrado: '$RuntimeJarPath'. Use -DownloadIfMissing para baixar do release oficial."
}

[pscustomobject]@{
    EvoSuiteVersion = $EvoSuiteVersion
    EvoSuiteJarPath = $EvoSuiteJarPath
    RuntimeJarPath = $RuntimeJarPath
}
