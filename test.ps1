$ErrorActionPreference = 'Stop'

$sources = @(
    Get-ChildItem -Path 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
    Get-ChildItem -Path 'src/test/java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
)
if (-not $sources) {
    throw 'No Java source files found.'
}

New-Item -ItemType Directory -Force -Path 'out-test' | Out-Null
javac -encoding UTF-8 -d out-test $sources
java -ea -cp out-test com.atm.ATMTest
