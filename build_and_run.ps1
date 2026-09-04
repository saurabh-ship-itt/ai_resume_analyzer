$ErrorActionPreference = "Stop"

Write-Host "Gathering JAR dependencies from .m2 repository..."
$repoPath = "$env:USERPROFILE\.m2\repository"

# Filter out legacy slf4j 1.x and deduplicate jars targeting 3.1.6
$rawJars = Get-ChildItem -Recurse -Filter "*.jar" $repoPath | Where-Object { $_.FullName -notmatch "slf4j-api-1\." }
$grouped = $rawJars | Group-Object { $_.Name -replace '-\d+.*\.jar$', '' }
$jarFiles = $grouped | ForEach-Object {
    $matched = $_.Group | Where-Object { $_.FullName -match "3\.1\.6" }
    if ($matched) { $matched[0].FullName }
    else { $_.Group[0].FullName }
}

$jars = ($jarFiles | ForEach-Object { $_ -replace '\\', '/' }) -join ";"
$lombok = (Get-ChildItem -Recurse -Filter "lombok*.jar" $repoPath | Select-Object -First 1 -ExpandProperty FullName) -replace '\\', '/'
$pwdPath = $PWD.Path -replace '\\', '/'

Write-Host "Found $($jarFiles.Count) deduplicated JARs in repository."

if (-not (Test-Path "target/classes")) {
    New-Item -ItemType Directory -Path "target/classes" -Force | Out-Null
}

$javaFiles = Get-ChildItem -Recurse -Filter "*.java" "src/main/java" | Select-Object -ExpandProperty FullName | ForEach-Object { $_ -replace '\\', '/' }

$javacArgs = @(
    "-encoding", "UTF-8",
    "-cp", "`"$jars`"",
    "-processorpath", "`"$lombok`"",
    "-d", "`"$pwdPath/target/classes`""
) + ($javaFiles | ForEach-Object { "`"$_`"" })

[System.IO.File]::WriteAllLines("$PWD/javac_args.txt", $javacArgs)

Write-Host "Compiling Java files..."
javac "@javac_args.txt"

Write-Host "Copying resources..."
Copy-Item -Path "src/main/resources/*" -Destination "target/classes" -Recurse -Force

Write-Host "Starting Spring Boot Application with H2 profile..."
$javaArgs = @(
    "-cp", "`"$pwdPath/target/classes;$jars`"",
    "com.example.resumeanalyzer.ResumeAnalyzerApplication",
    "--spring.profiles.active=h2"
)
[System.IO.File]::WriteAllLines("$PWD/java_args.txt", $javaArgs)

java "@java_args.txt"
