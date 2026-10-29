$ErrorActionPreference = 'Stop'

$sources = Get-ChildItem -Path 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
if (-not $sources) {
    throw 'No Java source files found under src/main/java.'
}

New-Item -ItemType Directory -Force -Path 'out' | Out-Null
javac -encoding UTF-8 -d out $sources
java -cp out com.atm.Main
