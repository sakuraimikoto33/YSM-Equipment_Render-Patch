# YSM Mapping API integration contract

Keep the consumer contract on Mapping API `0.1.0`, request schema `1`, and curated definition revision `1` unless the user explicitly authorizes a version change.

## Required surface

The patch requires these curated methods:

- `ysm.client.equipment.elytra_item_getter.method`
- `ysm.client.renderer.elytra_layer.render.method`
- `ysm.client.custom_player.entity_getter.method`
- `ysm.client.custom_player.current_model_getter.method`
- `ysm.client.animated_model.right_waist_bones_getter.method`
- `ysm.client.render_utils.prep_matrix_for_locator.method`

`ysm.client.animated_model.head_bones_getter.method` is optional; an unresolved value disables only Kaleidoscope Doll head rendering.

Only symbols whose stable aliases appear in compiled Mixin annotations belong in
`mixinRequirements`. Reflection-only symbols remain in `symbols` and are resolved through
`YsmMappingApi`; including them in `mixinRequirements` causes the Mapping API to reject the
Mixin because those symbols intentionally have no source alias.

## Runtime and distribution

Declare YSM-Mapping-API as a required loader dependency ordered before the patch, use the sibling repository through Gradle `includeBuild`, and compile against `net.okitsu.ysmmapping:api:0.1.0` without embedding it.

Mixin targets and methods must use stable `net.okitsu.ysmequipmentrenderpatch.ysmref` aliases declared in `requests-v1.json`. The Mapping API plugin and reference-mapper wrapper own conversion to runtime YSM names. Do not add direct `com.elfmcys.yesstevemodel` names, a second YSM JAR analyzer, or a private symbol cache.
