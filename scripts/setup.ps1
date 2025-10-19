<#
.SYNOPSIS
    Prepare the AI Trimmer Twitch development workspace on Windows.

.DESCRIPTION
    Creates a workspace directory, prompts for the FFmpeg executable path, and
    exports the AI_TRIMMER_WORKSPACE and FFMPEG_PATH environment variables. The
    values are saved to a .env file and optionally persisted for the current user.
#>
[CmdletBinding()]
param(
    [Parameter()]
    [string]$WorkspaceRoot = (Join-Path (Split-Path -Parent $PSScriptRoot) 'workspace'),

    [Parameter(HelpMessage = 'Persist environment variables for the current user')]
    [switch]$PersistEnv
)

Write-Host '=== AI Trimmer Twitch initial setup ===' -ForegroundColor Cyan

if (-not (Test-Path -LiteralPath $WorkspaceRoot)) {
    Write-Host "Creating workspace at '$WorkspaceRoot'."
    New-Item -ItemType Directory -Path $WorkspaceRoot -Force | Out-Null
}

$workspacePath = (Resolve-Path -LiteralPath $WorkspaceRoot).Path

$subFolders = @('clips', 'logs', 'temp')
foreach ($folder in $subFolders) {
    $fullPath = Join-Path $workspacePath $folder
    if (-not (Test-Path -LiteralPath $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
    }
}

function Get-FFmpegPath {
    param([string]$ExistingPath)

    while ($true) {
        $prompt = 'Enter the full path to ffmpeg.exe'
        if ($ExistingPath) {
            $prompt += " [`$ExistingPath`]"
        }
        $inputPath = Read-Host $prompt
        if ([string]::IsNullOrWhiteSpace($inputPath)) {
            if ($ExistingPath -and (Test-Path -LiteralPath $ExistingPath)) {
                return (Resolve-Path -LiteralPath $ExistingPath).Path
            }
            Write-Warning 'FFmpeg path is required.'
            continue
        }
        if (-not (Test-Path -LiteralPath $inputPath)) {
            Write-Warning "No file found at '$inputPath'."
            continue
        }
        if (-not ($inputPath.ToLowerInvariant().EndsWith('ffmpeg.exe'))) {
            Write-Warning 'The path must point to ffmpeg.exe'
            continue
        }
        return (Resolve-Path -LiteralPath $inputPath).Path
    }
}

$existingEnv = [Environment]::GetEnvironmentVariable('FFMPEG_PATH', 'Process')
if (-not $existingEnv) {
    $existingEnv = [Environment]::GetEnvironmentVariable('FFMPEG_PATH', 'User')
}
$ffmpegPath = Get-FFmpegPath -ExistingPath $existingEnv

$env:AI_TRIMMER_WORKSPACE = $workspacePath
$env:FFMPEG_PATH = $ffmpegPath
Write-Host "Set AI_TRIMMER_WORKSPACE to '$workspacePath'."
Write-Host "Set FFMPEG_PATH to '$ffmpegPath'."

$dotenvPath = Join-Path (Split-Path -Parent $PSScriptRoot) '.env'
"AI_TRIMMER_WORKSPACE=$workspacePath" | Set-Content -Path $dotenvPath -Encoding UTF8
"FFMPEG_PATH=$ffmpegPath" | Add-Content -Path $dotenvPath -Encoding UTF8
Write-Host "Session variables saved to $dotenvPath." -ForegroundColor Green

if ($PersistEnv) {
    [Environment]::SetEnvironmentVariable('AI_TRIMMER_WORKSPACE', $workspacePath, 'User')
    [Environment]::SetEnvironmentVariable('FFMPEG_PATH', $ffmpegPath, 'User')
    Write-Host 'Environment variables persisted for the current user.' -ForegroundColor Green
}

Write-Host 'Setup complete. You can now build the backend and frontend assets.' -ForegroundColor Cyan
