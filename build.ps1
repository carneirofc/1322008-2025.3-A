param(
    [ValidateSet("clean", "compile", "jar", "run")]
    [string]$Task = "jar",
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$AppArgs
)

$ErrorActionPreference = "Stop"

$JdkHome = "C:\Users\claud\microsoft-jdk-21.0.10-windows-x64\jdk-21.0.10+7"
$Javac = Join-Path $JdkHome "bin\javac.exe"
$Jar = Join-Path $JdkHome "bin\jar.exe"
$Java = Join-Path $JdkHome "bin\java.exe"

if (!(Test-Path $Javac)) {
    Write-Error "JDK not found at '$JdkHome'."
}

$env:JAVA_HOME = $JdkHome
$env:Path = "$JdkHome\bin;$env:Path"

$ProjectRoot = $PSScriptRoot
$SourceDir = Join-Path $ProjectRoot "src\main\java"
$TargetDir = Join-Path $ProjectRoot "target"
$ClassesDir = Join-Path $TargetDir "classes"
$SourceList = Join-Path $TargetDir "sources.txt"
$JarFile = Join-Path $TargetDir "calculator.jar"

function Invoke-Clean {
    if (Test-Path $TargetDir) {
        Remove-Item -Recurse -Force $TargetDir
    }
}

function Invoke-Compile {
    New-Item -ItemType Directory -Force -Path $ClassesDir | Out-Null
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null

    Get-ChildItem -Path $SourceDir -Recurse -Filter *.java |
        Select-Object -ExpandProperty FullName |
        Set-Content -Path $SourceList -Encoding ascii

    & $Javac --release 17 -d $ClassesDir "@$SourceList"
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

function Invoke-Jar {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
    if (Test-Path $JarFile) {
        Remove-Item -Force $JarFile
    }
    & $Jar --create --file $JarFile --main-class com.example.calculator.CalculatorApp -C $ClassesDir .
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

function Invoke-Run {
    & $Java -jar $JarFile @AppArgs
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

switch ($Task) {
    "clean" {
        Invoke-Clean
    }
    "compile" {
        Invoke-Compile
    }
    "jar" {
        Invoke-Compile
        Invoke-Jar
    }
    "run" {
        if (!(Test-Path $JarFile)) {
            Invoke-Compile
            Invoke-Jar
        }
        Invoke-Run
    }
}
