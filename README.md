# YSM Equipment Render Patch

- [English](#english)
- [日本語](#japanese)

<a id="english"></a>
## English

### Overview

**YSM Equipment Render Patch** is a patch mod that lets some equipment appear correctly when using Yes Steve Model.

It currently supports chest plates with Elytra functionality, the flight module when Draconic Evolution is installed, Lantern items equipped in Curios slots, and Kaleidoscope Doll items equipped in Curios head slots.

### Requirements

| What | Version |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.20+ |
| Yes Steve Model | 2.6.0-forge+mc1.20.1+ |
| YSM Mapping API | 0.1.0+ |
| Curios | 5.14.1+1.20.1+ — optional, for Curios slot equipment |
| Draconic Evolution | 3.1.2.621+ — optional, for flight module Elytra display |
| Kaleidoscope Doll | 1.4.1-forge+mc1.20.1+ — optional, for Curios head slot doll display |

### Features

- Displays Elytra while using a chest plate with Elytra functionality.
- Follows the Draconic Evolution flight module Elytra setting when Draconic Evolution is installed.
- Displays Lantern items equipped in Curios slots.
- Displays Kaleidoscope Doll items equipped in Curios head slots when Kaleidoscope Doll and Curios are installed.
- Items equipped in Curios slots can be hidden according to the Curios slot display settings.
- Applies swing physics to rendered lanterns.
- Curios slot equipment availability depends on the corresponding mod; this mod only implements rendering.

### Installation

Download `ysm-equipment-render-patch-mc<mc-version>-<version>.jar` and place it in the client-side `mods/` folder. Make sure Yes Steve Model and YSM Mapping API are installed.

### Building

1. Install JDK 17.
2. Clone this repository.
3. Run the build command:

```bash
./gradlew build
```

The built jar will be generated in `build/libs/`.

### Credits

The lantern swing behavior is based on [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns).

<a id="japanese"></a>
## 日本語

### 概要

**YSM Equipment Render Patch** は、Yes Steve Model 使用時に一部装備を正しく表示するためのパッチmodです。

現在は、エリトラ機能を持つチェストプレート、Draconic Evolution 導入時はフライトモジュール、Curios 導入時は Curios スロットに装備したランタン、Kaleidoscope Doll と Curios 導入時は Curios の head スロットに装備した Kaleidoscope Doll の描画に対応します。

### 要件

| 項目 | バージョン |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.20+ |
| Yes Steve Model | 2.6.0-forge+mc1.20.1+ |
| YSM Mapping API | 0.1.0+ |
| Curios | 5.14.1+1.20.1+ — 任意、Curios スロット装備用 |
| Draconic Evolution | 3.1.2.621+ — 任意、フライトモジュールのエリトラ表示用 |
| Kaleidoscope Doll | 1.4.1-forge+mc1.20.1+ — 任意、Curios head スロットのドール表示用 |

### 特徴

- エリトラ機能を持つチェストプレートを使用中にエリトラを表示します。
- Draconic Evolution 導入時、フライトモジュールのエリトラ設定に従って表示します。
- Curios 導入時、Curios スロットに装備したランタンを表示します。
- Kaleidoscope Doll と Curios 導入時、Curios の head スロットに装備した Kaleidoscope Doll を表示します。
- Curios スロットに装備するアイテムは Curios スロットの表示設定に従い非表示に出来ます。
- 表示されたランタンに揺れを実装します。
- Curios スロットに装備可能にする仕様は各対応 mod 側に依存します。この mod は描画のみを実装します。

### インストール方法

`ysm-equipment-render-patch-mc<mc-version>-<version>.jar`をダウンロードし、クライアント側の`mods/`フォルダに入れてください。Yes Steve Model と YSM Mapping API がインストールされていることを確認してください。

### ビルド方法

1. JDK 17 を導入します。
2. このリポジトリをクローンします。
3. 以下のコマンドを実行します。

```bash
./gradlew build
```

ビルドされた jar は `build/libs/` に生成されます。

### クレジット

ランタンの揺れ挙動は [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns) の揺れ方を利用しています。
