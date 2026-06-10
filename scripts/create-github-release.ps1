[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $Version,

    [string] $Repository = "Elykia093/Octopus-Mobile",
    [string] $AssetPath = "app/build/outputs/apk/release/app-release-unsigned.apk",
    [string] $GhPath = $(if ($env:GH_CLI_PATH) { $env:GH_CLI_PATH } else { "gh" }),
    [switch] $Signed,
    [switch] $SkipGitChecks,
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"
if ($PSVersionTable.PSVersion.Major -lt 6) {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
}

$versionNumber = $Version.Trim().TrimStart("v")
if ([string]::IsNullOrWhiteSpace($versionNumber)) {
    throw "Version cannot be empty."
}

$tag = "v$versionNumber"
$assetFlavor = if ($Signed) { "signed" } else { "unsigned" }
$assetName = "Octopus-Mobile-$tag-$assetFlavor.apk"

$changelogPath = Join-Path $PSScriptRoot "..\CHANGELOG.md"
$buildPath = Join-Path $PSScriptRoot "..\app\build.gradle.kts"
if (-not (Test-Path -LiteralPath $changelogPath)) {
    throw "CHANGELOG.md was not found."
}
if (-not (Test-Path -LiteralPath $buildPath)) {
    throw "app/build.gradle.kts was not found."
}

function Invoke-GitCheck {
    param(
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments,
        [Parameter(Mandatory = $true)]
        [string] $FailureMessage
    )

    $output = & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw $FailureMessage
    }
    return $output
}

if (-not $SkipGitChecks) {
    $status = Invoke-GitCheck -Arguments @("status", "--porcelain") -FailureMessage "Unable to inspect git working tree."
    if ($status) {
        throw "Git working tree is not clean. Commit or stash changes before creating a release."
    }

    $null = Invoke-GitCheck -Arguments @("rev-parse", "--verify", "refs/tags/$tag") -FailureMessage "Git tag $tag does not exist."
}

$build = Get-Content -LiteralPath $buildPath -Raw
$versionNamePattern = 'versionName\s*=\s*"' + [regex]::Escape($versionNumber) + '"'
if ($build -notmatch $versionNamePattern) {
    throw "app/build.gradle.kts versionName does not match $versionNumber."
}

$asset = Resolve-Path -LiteralPath $AssetPath -ErrorAction Stop
$changelog = Get-Content -LiteralPath $changelogPath -Raw
$escapedVersion = [regex]::Escape($versionNumber)
$sectionPattern = "(?ms)^## \[$escapedVersion\] - (?<date>[^\r\n]+)\r?\n(?<body>.*?)(?=^## \[|\z)"
$section = [regex]::Match($changelog, $sectionPattern)
if (-not $section.Success) {
    throw "CHANGELOG.md does not contain a section for [$versionNumber]."
}
$releaseDate = $section.Groups["date"].Value.Trim()
if ($releaseDate -eq "Unreleased") {
    throw "CHANGELOG.md section [$versionNumber] is still marked as Unreleased."
}

if (-not (Get-Command -Name $GhPath -ErrorAction SilentlyContinue)) {
    throw "GitHub CLI was not found at '$GhPath'. Set GH_CLI_PATH or pass -GhPath."
}

$notes = @"
## Octopus Mobile $tag

Release date: $releaseDate

$($section.Groups["body"].Value.Trim())

### Release Artifact

- $assetName
"@

$assetArgument = "$($asset.Path)#$assetName"
$ghArguments = @(
    "release",
    "create",
    $tag,
    $assetArgument,
    "--repo",
    $Repository,
    "--title",
    "Octopus Mobile $tag",
    "--notes-file",
    "",
    "--verify-tag"
)

if ($DryRun) {
    Write-Host "Release tag: $tag"
    Write-Host "Repository: $Repository"
    Write-Host "Asset: $assetArgument"
    Write-Host "GitHub CLI: $GhPath"
    Write-Host ""
    Write-Host $notes
    exit 0
}

$notesFile = New-TemporaryFile
try {
    Set-Content -LiteralPath $notesFile -Value $notes -Encoding UTF8
    $ghArguments[9] = $notesFile.FullName
    & $GhPath @ghArguments
    if ($LASTEXITCODE -ne 0) {
        throw "GitHub CLI exited with code $LASTEXITCODE."
    }
} finally {
    Remove-Item -LiteralPath $notesFile -Force -ErrorAction SilentlyContinue
}
