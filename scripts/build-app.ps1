param(
    [ValidateSet("clean", "compile", "jar")]
    [string]$Task = "jar"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$Mvn = Join-Path $ProjectRoot "vendor\tools\apache-maven-3.9.9-bin\apache-maven-3.9.9\bin\mvn.cmd"
Push-Location $ProjectRoot

try {
    switch ($Task) {
        "clean"   { & $Mvn clean }
        "compile" { & $Mvn compile }
        "jar"     { & $Mvn clean package -DskipTests }
    }

    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    if ($Task -eq "jar") {
        $Jar = Get-ChildItem -Path "target" -Filter "*.jar" | Select-Object -First 1
        if ($Jar) {
            Write-Host "Build concluido: target/$($Jar.Name)"
        }
    }
} finally {
    Pop-Location
}
