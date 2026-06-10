[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $Version,

    [string] $Repository = "Elykia093/Octopus-Mobile",
    [string] $AssetPath = "app/build/outputs/apk/release/app-release-unsigned.apk",
    [string] $GhPath = $(if ($env:GH_CLI_PATH) { $env:GH_CLI_PATH } else { "gh" }),
    [switch] $Signed,
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"

$versionNumber = $Version.Trim().TrimStart("v")
if ([string]::IsNullOrWhiteSpace($versionNumber)) {
    throw "Version cannot be empty."
}

$tag = "v$versionNumber"
$assetFlavor = if ($Signed) { "signed" } else { "unsigned" }
$assetName = "Octopus-Mobile-$tag-$assetFlavor.apk"

$changelogPath = Join-Path $PSScriptRoot "..\CHANGELOG.md"
if (-not (Test-Path -LiteralPath $changelogPath)) {
    throw "CHANGELOG.md was not found."
}

$asset = Resolve-Path -LiteralPath $AssetPath -ErrorAction Stop
$changelog = Get-Content -LiteralPath $changelogPath -Raw
$escapedVersion = [regex]::Escape($versionNumber)
$sectionPattern = "(?ms)^## \[$escapedVersion\] - (?<date>[^\r\n]+)\r?\n(?<body>.*?)(?=^## \[|\z)"
$section = [regex]::Match($changelog, $sectionPattern)
if (-not $section.Success) {
    throw "CHANGELOG.md does not contain a section for [$versionNumber]."
}

$notes = @"
## Octopus Mobile $tag

发布日期：$($section.Groups["date"].Value.Trim())

$($section.Groups["body"].Value.Trim())

### 发布产物

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
