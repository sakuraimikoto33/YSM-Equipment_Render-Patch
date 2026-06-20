# YSM Equipment Render Patch

- [English](#english)
- [日本語](#japanese)

<a id="english"></a>
## English

### Overview

YSM Equipment Render Patch is a patch mod that lets some equipment appear correctly when using Yes Steve Model.

It currently supports chest plates with Elytra functionality, the flight module when Draconic Evolution is installed, and Elytra and Lantern items equipped in Curios slots when Curios is installed.

### Requirements

| What | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228+ |
| Yes Steve Model | 2.6.0-neoforge+mc1.21.1+ |
| Curios | 9.5.1+1.21.1+ — optional, for Curios slot equipment |
| Draconic Evolution | 3.1.4.632+ — optional, for flight module Elytra display |

### Features

- Displays Elytra while using a chest plate with Elytra functionality.
- Follows the Draconic Evolution flight module Elytra setting when Draconic Evolution is installed.
- Displays Elytra and Lantern items equipped in Curios slots.
- Requires another mod to equip Elytra or Lantern items in Curios slots; this mod only implements rendering.
- Applies swing physics to rendered lanterns.

### Installation

Download `ysm-equipment-render-patch-<version>.jar` and place it in the client-side `mods/` folder. Make sure Yes Steve Model is installed.

### Building

1. Install JDK 21.
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

YSM Equipment Render Patch は、Yes Steve Model 使用時に一部装備を正しく表示するためのパッチmodです。

現在は、エリトラ機能を持つチェストプレート、Draconic Evolution 導入時は、フライトモジュール、Curios 導入時は、Curios スロットに装備したエリトラとランタンの描画に対応します。

### 要件

| 項目 | バージョン |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228+ |
| Yes Steve Model | 2.6.0-neoforge+mc1.21.1+ |
| Curios | 9.5.1+1.21.1+ — 任意、Curios スロット装備用 |
| Draconic Evolution | 3.1.4.632+ — 任意、フライトモジュールのエリトラ表示用 |

### 特徴

- エリトラ機能を持つチェストプレートを使用中にエリトラを表示します。
- Draconic Evolution 導入時、フライトモジュールのエリトラ設定に従って表示します。
- Curios 導入時、Curios スロットに装備したエリトラとランタンを表示します。
- エリトラやランタンを Curios スロットに装備するには別の mod が必要です。この mod は描画のみを実装します。
- 表示されたランタンに揺れを実装します。

### インストール方法

`ysm-equipment-render-patch-<version>.jar`をダウンロードし、クライアント側の`mods/`フォルダに入れてください。Yes Steve Model がインストールされていることを確認してください。

### ビルド方法

1. JDK 21 を導入します。
2. このリポジトリをクローンします。
3. 以下のコマンドを実行します。

```bash
./gradlew build
```

ビルドされた jar は `build/libs/` に生成されます。

### クレジット

ランタンの揺れ挙動は [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns) の揺れ方を利用しています。