[CmdletBinding()]
param(
    [string]$RepoRoot = "",
    [switch]$AllowContractVersionChange
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-RepositoryRoot {
    if ($RepoRoot) {
        return (Resolve-Path -LiteralPath $RepoRoot).Path
    }
    $candidate = @(& git -C $PSScriptRoot rev-parse --show-toplevel 2>&1)
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot locate repository root: $($candidate -join [Environment]::NewLine)"
    }
    (Resolve-Path -LiteralPath "$($candidate[-1])").Path
}

function Require-File {
    param([string]$Root, [string]$Relative, [Collections.Generic.List[string]]$Errors)
    $path = Join-Path $Root $Relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        $Errors.Add("Required file is missing: $Relative")
        return $null
    }
    return $path
}

function Read-Json {
    param([string]$Path, [string]$Label, [Collections.Generic.List[string]]$Errors)
    if (-not $Path) {
        return $null
    }
    try {
        return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json
    } catch {
        $Errors.Add("$Label is not valid JSON: $($_.Exception.Message)")
        return $null
    }
}

function Require-Text {
    param([string]$Text, [string]$Pattern, [string]$Message,
        [Collections.Generic.List[string]]$Errors)
    if ($Text -notmatch $Pattern) {
        $Errors.Add($Message)
    }
}

try {
    $root = Resolve-RepositoryRoot
    $branchOutput = @(& git -C $root branch --show-current 2>&1)
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot determine active branch: $($branchOutput -join [Environment]::NewLine)"
    }
    $branch = "$($branchOutput[-1])"
    $isMinecraft = $branch -match '^mc/'
    $errors = [Collections.Generic.List[string]]::new()

    if ($isMinecraft) {
        $settingsPath = Require-File $root "settings.gradle" $errors
        $buildPath = Require-File $root "build.gradle" $errors
        $manifestRelative = "src/main/resources/META-INF/ysm-mapping-api/requests-v1.json"
        $manifestPath = Require-File $root $manifestRelative $errors
        $mixinRelative = "src/main/resources/ysm_equipment_render_patch.mixins.json"
        $mixinPath = Require-File $root $mixinRelative $errors

        if ($settingsPath) {
            $settings = Get-Content -Raw -LiteralPath $settingsPath
            Require-Text $settings 'includeBuild\s*\(\s*[''"]\.\./YSM-Mapping-API[''"]\s*\)' "settings.gradle must include ../YSM-Mapping-API." $errors
        }
        if ($buildPath) {
            $build = Get-Content -Raw -LiteralPath $buildPath
            Require-Text $build 'net\.okitsu\.ysmmapping:api:0\.1\.0' "build.gradle must compile against Mapping API 0.1.0." $errors
        }

        $metadataRelative = if (Test-Path -LiteralPath (Join-Path $root "src/main/resources/META-INF/neoforge.mods.toml")) {
            "src/main/resources/META-INF/neoforge.mods.toml"
        } else {
            "src/main/resources/META-INF/mods.toml"
        }
        $metadataPath = Require-File $root $metadataRelative $errors
        if ($metadataPath) {
            $metadata = Get-Content -Raw -LiteralPath $metadataPath
            Require-Text $metadata 'modId\s*=\s*"ysm_mapping_api"' "$metadataRelative must require ysm_mapping_api." $errors
            Require-Text $metadata 'versionRange\s*=\s*"\[0\.1\.0,\)"' "$metadataRelative must keep the Mapping API dependency floor at 0.1.0." $errors
            Require-Text $metadata 'ordering\s*=\s*"BEFORE"' "$metadataRelative must order Mapping API before the patch." $errors
        }

        $required = @(
            "ysm.client.equipment.elytra_item_getter.method"
            "ysm.client.renderer.elytra_layer.render.method"
            "ysm.client.custom_player.entity_getter.method"
            "ysm.client.custom_player.current_model_getter.method"
            "ysm.client.animated_model.right_waist_bones_getter.method"
            "ysm.client.render_utils.prep_matrix_for_locator.method"
        )
        $optional = "ysm.client.animated_model.head_bones_getter.method"
        $manifest = Read-Json $manifestPath $manifestRelative $errors
        if ($manifest) {
            if ($manifest.schemaVersion -ne 1) {
                $errors.Add("Request manifest schemaVersion must remain 1.")
            }
            $symbols = @($manifest.symbols)
            $keys = @($symbols | ForEach-Object { [string]$_.key })
            if (@($keys | Sort-Object -Unique).Count -ne $keys.Count) {
                $errors.Add("Request manifest contains duplicate symbol keys.")
            }
            foreach ($key in $required) {
                $entry = @($symbols | Where-Object { $_.key -eq $key })
                if ($entry.Count -ne 1 -or [string]$entry[0].kind -ne "METHOD" -or
                        -not [bool]$entry[0].required) {
                    $errors.Add("Required METHOD symbol is missing or invalid: $key")
                }
            }
            $optionalEntry = @($symbols | Where-Object { $_.key -eq $optional })
            if ($optionalEntry.Count -ne 1 -or [string]$optionalEntry[0].kind -ne "METHOD" -or
                    [bool]$optionalEntry[0].required) {
                $errors.Add("Optional head-bones METHOD symbol is missing or invalid.")
            }

            $aliases = @{
                "ysm.client.equipment.elytra_item_getter.method" = @{
                    owner = "net/okitsu/ysmequipmentrenderpatch/ysmref/EquipmentLookup"
                    name = "elytraItemGetter"
                    descriptor = "(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
                }
                "ysm.client.renderer.elytra_layer.render.method" = @{
                    owner = "net/okitsu/ysmequipmentrenderpatch/ysmref/ElytraLayer"
                    name = "render"
                    descriptor = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/okitsu/ysmequipmentrenderpatch/ysmref/CustomPlayer;FFFFFF)V"
                }
            }
            foreach ($key in $aliases.Keys) {
                $entry = @($symbols | Where-Object { $_.key -eq $key })[0]
                $common = $entry.sourceAlias.common
                foreach ($field in @("owner", "name", "descriptor")) {
                    if ([string]$common.$field -cne [string]$aliases[$key][$field]) {
                        $errors.Add("Source alias mismatch for " + $key + ": " + $field)
                    }
                }
            }

            $requirements = @{
                "net.okitsu.ysmequipmentrenderpatch.mixin.YsmElytraEquipmentMixin" = @(
                    "ysm.client.equipment.elytra_item_getter.method"
                )
                "net.okitsu.ysmequipmentrenderpatch.mixin.YsmElytraLayerMixin" = @(
                    "ysm.client.renderer.elytra_layer.render.method"
                    "ysm.client.custom_player.entity_getter.method"
                    "ysm.client.custom_player.current_model_getter.method"
                    "ysm.client.animated_model.right_waist_bones_getter.method"
                    "ysm.client.render_utils.prep_matrix_for_locator.method"
                )
            }
            foreach ($mixinName in $requirements.Keys) {
                $property = $manifest.mixinRequirements.PSObject.Properties[$mixinName]
                if ($null -eq $property) {
                    $errors.Add("Mixin requirement is missing: $mixinName")
                    continue
                }
                $actual = @($property.Value)
                foreach ($key in $requirements[$mixinName]) {
                    if ($actual -notcontains $key) {
                        $errors.Add("Mixin requirement $mixinName is missing $key.")
                    }
                }
            }
        }

        $mixin = Read-Json $mixinPath $mixinRelative $errors
        if ($mixin) {
            if ([string]$mixin.plugin -cne "net.okitsu.ysmmapping.internal.mixin.YsmMappingMixinPlugin") {
                $errors.Add("Mixin config must use the Mapping API plugin.")
            }
            if ([string]$mixin.refmapWrapper -cne "YsmReferenceMapper") {
                $errors.Add("Mixin config must use YsmReferenceMapper.")
            }
            foreach ($name in @("YsmElytraEquipmentMixin", "YsmElytraLayerMixin")) {
                if (@($mixin.client) -notcontains $name) {
                    $errors.Add("Mixin config is missing client mixin: $name")
                }
            }
        }

        foreach ($legacy in @(
            "src/main/resources/META-INF/services/cpw.mods.modlauncher.api.ITransformationService"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/launch/YsmEquipmentTransformer.java"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/launch/YsmJarLocator.java"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/launch/YsmServiceBridge.java"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/launch/YsmSymbolAnalyzer.java"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/launch/YsmSymbolDetectionService.java"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/runtime/YsmRuntimeSymbolCache.java"
            "src/main/java/net/okitsu/ysmequipmentrenderpatch/runtime/YsmRuntimeSymbols.java"
        )) {
            if (Test-Path -LiteralPath (Join-Path $root $legacy)) {
                $errors.Add("Superseded private mapping implementation remains: $legacy")
            }
        }

        $sourceRoot = Join-Path $root "src"
        if (Test-Path -LiteralPath $sourceRoot) {
            $privateNames = Get-ChildItem -LiteralPath $sourceRoot -Recurse -File |
                Select-String -Pattern 'com[./]elfmcys[./]yesstevemodel' -List
            if ($privateNames) {
                $errors.Add("Direct proprietary YSM runtime names remain in source or resources.")
            }
        }
    }

    $result = [ordered]@{
        operation = "VerifyMappingIntegration"
        success = $errors.Count -eq 0
        branch = $branch
        minecraftBranch = $isMinecraft
        mappingApiVersion = "0.1.0"
        requestSchema = 1
        authorizedVersionChange = [bool]$AllowContractVersionChange
        errors = @($errors)
    }
    $result | ConvertTo-Json -Depth 6 -Compress
    if ($errors.Count -ne 0) {
        exit 1
    }
} catch {
    [ordered]@{
        operation = "VerifyMappingIntegration"
        success = $false
        error = $_.Exception.Message
    } | ConvertTo-Json -Compress
    exit 1
}
