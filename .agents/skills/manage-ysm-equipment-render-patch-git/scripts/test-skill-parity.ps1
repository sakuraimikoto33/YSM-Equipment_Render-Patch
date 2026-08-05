[CmdletBinding()]
param(
    [string]$VerifierScript = (Join-Path $PSScriptRoot "verify-skill-parity.ps1")
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$verifier = (Resolve-Path -LiteralPath $VerifierScript).Path
$current = @(& git -C $PSScriptRoot rev-parse --show-toplevel 2>&1)
if ($LASTEXITCODE -ne 0) {
    throw "Cannot locate source repositories: $($current -join [Environment]::NewLine)"
}
$equipmentSource = "$($current[-1])"
$parent = Split-Path -Parent $equipmentSource
$serverlessSource = Join-Path $parent "Serverless-YSM"
$mappingSource = Join-Path $parent "YSM-Mapping-API"
$testRoot = Join-Path ([IO.Path]::GetTempPath()) (
    "equipment-skill-parity-tests-" + [guid]::NewGuid().ToString("N"))
$pwsh = (Get-Process -Id $PID).Path
$script:Passed = 0

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
    $script:Passed++
}

function Invoke-Parity {
    param(
        [string]$Equipment,
        [string]$Serverless,
        [string]$Mapping,
        [switch]$ExpectFailure
    )
    $output = @(& $pwsh -NoProfile -File $verifier -EquipmentRoot $Equipment -ServerlessRoot $Serverless -MappingRoot $Mapping 2>&1 | ForEach-Object { "$_" })
    $code = $LASTEXITCODE
    if (-not $ExpectFailure -and $code -ne 0) {
        throw "Parity verifier failed: $($output -join [Environment]::NewLine)"
    }
    if ($ExpectFailure -and $code -eq 0) {
        throw "Parity verifier unexpectedly succeeded."
    }
    [pscustomobject]@{
        ExitCode = $code
        Json = $output[-1] | ConvertFrom-Json
    }
}

function Assert-DetectedFailure {
    param(
        [string]$Equipment,
        [string]$Serverless,
        [string]$Mapping,
        [string]$Pattern
    )
    $result = Invoke-Parity $Equipment $Serverless $Mapping -ExpectFailure
    Assert-True (-not $result.Json.success -and $result.Json.error -match $Pattern) "Expected parity failure '$Pattern' was not reported."
}

function Mutate-And-Restore {
    param([string]$Path, [scriptblock]$Mutation, [scriptblock]$Assertion)
    $bytes = [IO.File]::ReadAllBytes($Path)
    try {
        & $Mutation $Path
        & $Assertion
    } finally {
        [IO.File]::WriteAllBytes($Path, $bytes)
    }
}

try {
    $actual = Invoke-Parity $equipmentSource $serverlessSource $mappingSource
    Assert-True $actual.Json.success "Actual repository parity failed."
    Assert-True (@($actual.Json.excluded).Count -eq 4) "Excluded repository inputs were not reported."

    $equipment = Join-Path $testRoot "YSM-Equipment_Render-Patch"
    $serverless = Join-Path $testRoot "Serverless-YSM"
    $mapping = Join-Path $testRoot "YSM-Mapping-API"
    [void](New-Item -ItemType Directory -Force -Path $equipment, $serverless, $mapping)
    Copy-Item -LiteralPath (Join-Path $equipmentSource ".agents") -Destination $equipment -Recurse
    Copy-Item -LiteralPath (Join-Path $equipmentSource "AGENTS.md") -Destination $equipment
    Copy-Item -LiteralPath (Join-Path $serverlessSource ".agents") -Destination $serverless -Recurse
    Copy-Item -LiteralPath (Join-Path $serverlessSource "AGENTS.md") -Destination $serverless
    Copy-Item -LiteralPath (Join-Path $mappingSource ".agents") -Destination $mapping -Recurse
    Copy-Item -LiteralPath (Join-Path $mappingSource "AGENTS.md") -Destination $mapping
    Assert-True ((Invoke-Parity $equipment $serverless $mapping).Json.success) "Copied fixture parity failed."

    $workflow = Join-Path $equipment ".agents/skills/manage-ysm-equipment-render-patch-git/scripts/repository-workflow.ps1"
    Mutate-And-Restore $workflow {
        param($path)
        [IO.File]::AppendAllText($path, [Environment]::NewLine + "# workflow drift")
    } {
        Assert-DetectedFailure $equipment $serverless $mapping "Content parity mismatch"
    }

    $manageSkill = Join-Path $equipment ".agents/skills/manage-ysm-equipment-render-patch-git/SKILL.md"
    Mutate-And-Restore $manageSkill {
        param($path)
        [IO.File]::AppendAllText($path, [Environment]::NewLine + "Instruction drift")
    } {
        Assert-DetectedFailure $equipment $serverless $mapping "Normalized instruction mismatch"
    }

    [IO.File]::AppendAllText(
        (Join-Path $equipment ".agents/repository-profile.psd1"),
        [Environment]::NewLine + "# excluded profile drift")
    [IO.File]::AppendAllText(
        (Join-Path $equipment ".agents/skills/manage-ysm-equipment-render-patch-git/references/branch-ownership.md"),
        [Environment]::NewLine + "Excluded ownership drift")
    Assert-True ((Invoke-Parity $equipment $serverless $mapping).Json.success) "Excluded repository inputs affected parity."

    [ordered]@{
        operation = "TestSkillParity"
        passed = $script:Passed
        status = "PASS"
    } | ConvertTo-Json -Compress
} finally {
    if (Test-Path -LiteralPath $testRoot) {
        $resolved = [IO.Path]::GetFullPath($testRoot)
        $temp = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
        if ($resolved.StartsWith($temp, [StringComparison]::OrdinalIgnoreCase)) {
            Remove-Item -LiteralPath $resolved -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}
