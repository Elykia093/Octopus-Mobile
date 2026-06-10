[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $Version,

    [int] $VersionCode = 0,
    [string] $ReleaseDate = $(Get-Date -Format "yyyy-MM-dd"),
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"

$versionNumber = $Version.Trim().TrimStart("v")
if ([string]::IsNullOrWhiteSpace($versionNumber)) {
    throw "Version cannot be empty."
}

if ($versionNumber -notmatch "^\d+\.\d+\.\d+$") {
    throw "Version must use semantic version format, for example 0.5.0."
}

if ($ReleaseDate -notmatch "^\d{4}-\d{2}-\d{2}$") {
    throw "ReleaseDate must use yyyy-MM-dd format."
}

$buildPath = Join-Path $PSScriptRoot "..\app\build.gradle.kts"
$changelogPath = Join-Path $PSScriptRoot "..\CHANGELOG.md"

if (-not (Test-Path -LiteralPath $buildPath)) {
    throw "app/build.gradle.kts was not found."
}
if (-not (Test-Path -LiteralPath $changelogPath)) {
    throw "CHANGELOG.md was not found."
}

function Replace-RequiredOnce {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Text,
        [Parameter(Mandatory = $true)]
        [string] $Pattern,
        [Parameter(Mandatory = $true)]
        [string] $Replacement,
        [Parameter(Mandatory = $true)]
        [string] $Description
    )

    $regex = [regex]::new($Pattern)
    $matches = $regex.Matches($Text)
    if ($matches.Count -ne 1) {
        throw "Expected exactly one $Description match, found $($matches.Count)."
    }
    return $regex.Replace($Text, $Replacement, 1)
}

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Path,
        [Parameter(Mandatory = $true)]
        [string] $Text
    )

    $resolvedPath = Resolve-Path -LiteralPath $Path -ErrorAction Stop
    $encoding = New-Object System.Text.UTF8Encoding($false)
    $normalizedText = $Text.TrimEnd("`r", "`n") + [Environment]::NewLine
    [System.IO.File]::WriteAllText($resolvedPath.Path, $normalizedText, $encoding)
}

$build = Get-Content -LiteralPath $buildPath -Raw
$currentCodeMatch = [regex]::Match($build, "versionCode\s*=\s*(?<code>\d+)")
if (-not $currentCodeMatch.Success) {
    throw "versionCode was not found."
}
$currentVersionNameMatch = [regex]::Match($build, 'versionName\s*=\s*"(?<name>[^"]+)"')
if (-not $currentVersionNameMatch.Success) {
    throw "versionName was not found."
}

$targetVersionCode = if ($VersionCode -gt 0) {
    $VersionCode
} elseif ($currentVersionNameMatch.Groups["name"].Value -eq $versionNumber) {
    [int] $currentCodeMatch.Groups["code"].Value
} else {
    [int] $currentCodeMatch.Groups["code"].Value + 1
}

$nextBuild = Replace-RequiredOnce `
    -Text $build `
    -Pattern "versionCode\s*=\s*\d+" `
    -Replacement "versionCode = $targetVersionCode" `
    -Description "versionCode"
$nextBuild = Replace-RequiredOnce `
    -Text $nextBuild `
    -Pattern 'versionName\s*=\s*"[^"]+"' `
    -Replacement "versionName = `"$versionNumber`"" `
    -Description "versionName"

$changelog = Get-Content -LiteralPath $changelogPath -Raw
$escapedVersion = [regex]::Escape($versionNumber)
$headingPattern = "(?m)^## \[$escapedVersion\] - (Unreleased|\d{4}-\d{2}-\d{2})$"
$nextChangelog = Replace-RequiredOnce `
    -Text $changelog `
    -Pattern $headingPattern `
    -Replacement "## [$versionNumber] - $ReleaseDate" `
    -Description "CHANGELOG heading for [$versionNumber]"

if ($DryRun) {
    Write-Host "Prepare release: v$versionNumber"
    Write-Host "Release date: $ReleaseDate"
    Write-Host "Version code: $targetVersionCode"
    Write-Host "Build file: $buildPath"
    Write-Host "Changelog: $changelogPath"
    Write-Host ""
    Write-Host "Planned changes:"
    Write-Host "- app/build.gradle.kts versionCode -> $targetVersionCode"
    Write-Host "- app/build.gradle.kts versionName -> $versionNumber"
    Write-Host "- CHANGELOG.md [$versionNumber] date -> $ReleaseDate"
    exit 0
}

Write-Utf8NoBom -Path $buildPath -Text $nextBuild
Write-Utf8NoBom -Path $changelogPath -Text $nextChangelog

Write-Host "Prepared release v$versionNumber."
