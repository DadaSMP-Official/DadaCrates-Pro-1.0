$ErrorActionPreference = "Stop"
$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$libRoot = "C:\Crafty\servers\8d92b4b4-14a8-4f79-89d2-d8738658c38b\libraries"
$api = Join-Path $libRoot "io\papermc\paper\paper-api\1.20.1-R0.1-SNAPSHOT\paper-api-1.20.1-R0.1-SNAPSHOT.jar"
$target = Join-Path $base "target"
$classes = Join-Path $target "classes"
Remove-Item -Recurse -Force $target -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = Get-ChildItem (Join-Path $base "src\main\java") -Recurse -Filter *.java | ForEach-Object FullName
$classpath = @($api) + (Get-ChildItem $libRoot -Recurse -Filter *.jar | ForEach-Object FullName)
$classpath = ($classpath | Select-Object -Unique) -join ";"
& javac -encoding UTF-8 -source 17 -target 17 -cp $classpath -d $classes $sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Copy-Item (Join-Path $base "src\main\resources\*") $classes -Recurse -Force
& jar cf (Join-Path $target "DadaCratesPro-1.0.1-HOSTMR.jar") -C $classes .
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Built:"
Write-Host (Join-Path $target "DadaCratesPro-1.0.1-HOSTMR.jar")
