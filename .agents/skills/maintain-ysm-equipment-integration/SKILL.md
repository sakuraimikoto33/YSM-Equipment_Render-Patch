---
name: maintain-ysm-equipment-integration
description: Maintain and validate YSM-Equipment_Render-Patch integration with YSM-Mapping-API, including Gradle composite-build substitution, runtime mod dependencies, requests-v1.json consumer manifests, Mapping API-backed runtime symbols, Mixin source aliases, refmap wrappers, and removal of private symbol scanners or caches. Use for Mapping API dependency, manifest, Mixin, runtime mapping, or distribution contract work. Do not use for Git workflow alone.
---

# Maintain YSM Equipment Integration

Read [integration-contract.md](references/integration-contract.md) before changing the Mapping API integration.

## Workflow

1. Run `scripts/verify-mapping-integration.ps1` before editing and record any expected legacy failures.
2. Keep the Gradle included build, compile-only API dependency, loader metadata, request manifest, Mixin configuration, and Java symbol list aligned.
3. Use curated `YsmSymbols` keys and stable consumer aliases. Never store or publish private-derived YSM owner or member names.
4. Keep runtime mapping failures isolated: required mixins must be skipped safely, while optional rendering features may degrade independently.
5. Remove superseded JAR discovery, bytecode analysis, and private cache code rather than maintaining a parallel mapping source.
6. Run the verifier, the active branch build, and repository `Validate` after editing.
7. Report dependency, compatibility, and distribution effects. Do not change schema, definition, API, or release versions without explicit authorization.

```powershell
& .\.agents\skills\maintain-ysm-equipment-integration\scripts\verify-mapping-integration.ps1
```
