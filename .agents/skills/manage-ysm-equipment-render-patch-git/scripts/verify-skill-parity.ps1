[CmdletBinding()]
param(
    [string]$EquipmentRoot = "",
    [string]$ServerlessRoot = "",
    [string]$MappingRoot = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-Roots {
    if ($EquipmentRoot -or $ServerlessRoot -or $MappingRoot) {
        if (-not ($EquipmentRoot -and $ServerlessRoot -and $MappingRoot)) {
            throw "Specify all three repository roots, or none."
        }
        return [ordered]@{
            Equipment = (Resolve-Path -LiteralPath $EquipmentRoot).Path
            Serverless = (Resolve-Path -LiteralPath $ServerlessRoot).Path
            Mapping = (Resolve-Path -LiteralPath $MappingRoot).Path
        }
    }
    $current = @(& git -C $PSScriptRoot rev-parse --show-toplevel 2>&1)
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot locate the current repository: $($current -join [Environment]::NewLine)"
    }
    $current = "$($current[-1])"
    $parent = Split-Path -Parent $current
    [ordered]@{
        Equipment = (Resolve-Path -LiteralPath (Join-Path $parent "YSM-Equipment_Render-Patch")).Path
        Serverless = (Resolve-Path -LiteralPath (Join-Path $parent "Serverless-YSM")).Path
        Mapping = (Resolve-Path -LiteralPath (Join-Path $parent "YSM-Mapping-API")).Path
    }
}

function Resolve-RequiredFile {
    param([string]$Root, [string]$Relative)
    $path = Join-Path $Root $Relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required parity file is missing: $path"
    }
    (Resolve-Path -LiteralPath $path).Path
}

function Normalize-ContentText {
    param([string]$Path)
    [IO.File]::ReadAllText($Path) -replace '\r\n?', [string][char]10
}

function Normalize-InstructionText {
    param([string]$Path)
    $text = Normalize-ContentText $Path
    foreach ($pair in @(
        @("manage-serverless-ysm-git", "<git-skill>"),
        @("manage-ysm-mapping-api-git", "<git-skill>"),
        @("manage-ysm-equipment-render-patch-git", "<git-skill>"),
        @("rewrite-serverless-ysm-history", "<history-skill>"),
        @("rewrite-ysm-mapping-api-history", "<history-skill>"),
        @("rewrite-ysm-equipment-render-patch-history", "<history-skill>"),
        @("Serverless-YSM", "<repository>"),
        @("YSM-Mapping-API", "<repository>"),
        @("YSM-Equipment_Render-Patch", "<repository>"),
        @("Serverless YSM", "<repository>"),
        @("YSM Mapping API", "<repository>"),
        @("YSM Equipment Render Patch", "<repository>"),
        @("Minecraft branches", "branches")
    )) {
        $text = $text.Replace($pair[0], $pair[1])
    }
    $text
}

function Normalize-AgentsText {
    param([string]$Path)
    $text = Normalize-InstructionText $Path
    $text = [regex]::Replace($text,
        '(?m)^- Completion alone never authorizes a commit\..*$',
        '- Completion alone never authorizes a commit. Load $<git-skill> for pending work, task changes, branches, commits, merges, or pushes.')
    $lines = @($text -split [string][char]10 | Where-Object {
        $_ -notmatch '\$maintain-' -and $_ -notmatch '\$analyze-' -and
        $_ -notmatch '^- Never bump a mod/release' -and
        $_ -notmatch '^- Never track or distribute proprietary'
    })
    ($lines -join [string][char]10).TrimEnd() + [string][char]10
}

try {
    $roots = Resolve-Roots
    $baselineVerifier = Resolve-RequiredFile $roots.Serverless ".agents/skills/manage-serverless-ysm-git/scripts/verify-skill-parity.ps1"
    $baselineOutput = @(& $baselineVerifier -ServerlessRoot $roots.Serverless -MappingRoot $roots.Mapping 2>&1 | ForEach-Object { "$_" })
    $baselineSucceeded = $?
    if (-not $baselineSucceeded) {
        throw "Serverless/Mapping baseline parity failed: $($baselineOutput -join [Environment]::NewLine)"
    }
    $baseline = $baselineOutput[-1] | ConvertFrom-Json
    if (-not $baseline.success) {
        throw "Serverless/Mapping baseline parity did not report success."
    }

    $exactPairs = @(
        @(".agents/skills/manage-ysm-equipment-render-patch-git/scripts/repository-policy.psd1",
            ".agents/skills/manage-serverless-ysm-git/scripts/repository-policy.psd1"),
        @(".agents/skills/manage-ysm-equipment-render-patch-git/references/task-boundaries.md",
            ".agents/skills/manage-serverless-ysm-git/references/task-boundaries.md"),
        @(".agents/skills/manage-ysm-equipment-render-patch-git/scripts/repository-workflow.ps1",
            ".agents/skills/manage-serverless-ysm-git/scripts/repository-workflow.ps1"),
        @(".agents/skills/manage-ysm-equipment-render-patch-git/scripts/test-repository-workflow.ps1",
            ".agents/skills/manage-serverless-ysm-git/scripts/test-repository-workflow.ps1"),
        @(".agents/skills/rewrite-ysm-equipment-render-patch-history/references/rewrite-policy.md",
            ".agents/skills/rewrite-serverless-ysm-history/references/rewrite-policy.md"),
        @(".agents/skills/rewrite-ysm-equipment-render-patch-history/scripts/history-safety.ps1",
            ".agents/skills/rewrite-serverless-ysm-history/scripts/history-safety.ps1"),
        @(".agents/skills/rewrite-ysm-equipment-render-patch-history/scripts/test-history-safety.ps1",
            ".agents/skills/rewrite-serverless-ysm-history/scripts/test-history-safety.ps1")
    )
    $normalizedPairs = @(
        @(".agents/skills/manage-ysm-equipment-render-patch-git/SKILL.md",
            ".agents/skills/manage-serverless-ysm-git/SKILL.md"),
        @(".agents/skills/manage-ysm-equipment-render-patch-git/agents/openai.yaml",
            ".agents/skills/manage-serverless-ysm-git/agents/openai.yaml"),
        @(".agents/skills/rewrite-ysm-equipment-render-patch-history/SKILL.md",
            ".agents/skills/rewrite-serverless-ysm-history/SKILL.md"),
        @(".agents/skills/rewrite-ysm-equipment-render-patch-history/agents/openai.yaml",
            ".agents/skills/rewrite-serverless-ysm-history/agents/openai.yaml")
    )

    $exactResults = [Collections.Generic.List[object]]::new()
    foreach ($pair in $exactPairs) {
        $left = Resolve-RequiredFile $roots.Equipment $pair[0]
        $right = Resolve-RequiredFile $roots.Serverless $pair[1]
        $leftText = Normalize-ContentText $left
        $rightText = Normalize-ContentText $right
        if ($leftText -cne $rightText) {
            throw "Content parity mismatch: $($pair[0]) <> $($pair[1])."
        }
        $bytes = [Text.Encoding]::UTF8.GetBytes($leftText)
        $exactResults.Add([ordered]@{
            equipment = $pair[0]
            serverless = $pair[1]
            normalizedSha256 = [Convert]::ToHexString(
                [Security.Cryptography.SHA256]::HashData($bytes))
        })
    }

    $normalizedResults = [Collections.Generic.List[object]]::new()
    foreach ($pair in $normalizedPairs) {
        $left = Resolve-RequiredFile $roots.Equipment $pair[0]
        $right = Resolve-RequiredFile $roots.Serverless $pair[1]
        $leftText = Normalize-InstructionText $left
        $rightText = Normalize-InstructionText $right
        if ($leftText -cne $rightText) {
            throw "Normalized instruction mismatch: $($pair[0]) <> $($pair[1])."
        }
        $bytes = [Text.Encoding]::UTF8.GetBytes($leftText)
        $normalizedResults.Add([ordered]@{
            equipment = $pair[0]
            serverless = $pair[1]
            normalizedSha256 = [Convert]::ToHexString(
                [Security.Cryptography.SHA256]::HashData($bytes))
        })
    }

    $equipmentAgents = Resolve-RequiredFile $roots.Equipment "AGENTS.md"
    $serverlessAgents = Resolve-RequiredFile $roots.Serverless "AGENTS.md"
    $equipmentAgentsText = Normalize-AgentsText $equipmentAgents
    $serverlessAgentsText = Normalize-AgentsText $serverlessAgents
    if ($equipmentAgentsText -cne $serverlessAgentsText) {
        throw "Normalized instruction mismatch: AGENTS.md <> AGENTS.md."
    }
    [void](Resolve-RequiredFile $roots.Equipment ".agents/active-minecraft-branches.txt")

    [ordered]@{
        operation = "VerifySkillParity"
        success = $true
        baseline = "Serverless-YSM <> YSM-Mapping-API"
        exact = @($exactResults)
        normalized = @($normalizedResults)
        excluded = @(
            ".agents/repository-profile.psd1"
            ".agents/skills/manage-ysm-equipment-render-patch-git/references/branch-ownership.md"
            ".agents/active-minecraft-branches.txt"
            ".agents/skills/maintain-ysm-equipment-integration"
        )
    } | ConvertTo-Json -Depth 8 -Compress
} catch {
    [ordered]@{
        operation = "VerifySkillParity"
        success = $false
        error = $_.Exception.Message
    } | ConvertTo-Json -Compress
    exit 1
}
