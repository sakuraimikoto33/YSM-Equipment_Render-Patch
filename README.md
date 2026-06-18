# YSM Equipment Render Patch

- [English](#english)
- [日本語](#japanese)

<a id="english"></a>
## English

### Overview

YSM Equipment Render Patch is a small client-side compatibility patch that lets supported equipment appear correctly when using Yes Steve Model.

It currently supports rendering chest equipment with Elytra functionality. When Curios is installed, it also supports Elytra and Lantern items equipped in Curios slots.

### Requirements

| What | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228+ |
| Yes Steve Model | 2.6.5-neoforge+mc1.21.1 |
| Curios | 9.5.1+1.21.1+ — optional, for Curios slot equipment |

### Features

- Displays chest equipment with Elytra functionality on the active YSM model.
- Displays Elytra and Lantern items equipped through Curios on the active YSM model when Curios is installed.
- Requires another mod to equip Elytra or Lantern items in Curios slots; this mod only patches rendering.
- Applies simple swing physics to rendered lanterns.
- Works as a lightweight client-side render patch without adding new gameplay mechanics.

### Installation

Download `ysm-equipment-render-patch-<version>.jar` and place it in the client-side `mods/` folder. Make sure Yes Steve Model is installed. Install Curios and a compatible equipment-slot mod only when using Curios slot Elytra or Lantern items.

### Building

1. Install JDK 21.
2. Clone or download this repository.
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

YSM Equipment Render Patch は、Yes Steve Model 使用時に対応装備を正しく表示するための、小さなクライアント側互換パッチです。

現在は、エリトラ機能を持つチェスト装備の描画に対応しています。Curios 導入時は、Curios スロットに装備したエリトラとランタンの描画にも対応します。

### 要件

| 項目 | バージョン |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228+ |
| Yes Steve Model | 2.6.5-neoforge+mc1.21.1 |
| Curios | 9.5.1+1.21.1+ — 任意、Curios スロット装備用 |

### 特徴

- エリトラ機能を持つチェスト装備を使用中の YSM モデル上に表示します。
- Curios 導入時、Curios に装備したエリトラとランタンを使用中の YSM モデル上に表示します。
- エリトラやランタンを Curios スロットに装備するには別の mod が必要です。この mod は描画のみを補正します。
- 表示されたランタンに簡易的な揺れを適用します。
- 新しいゲーム要素を追加せず、描画だけを補正する軽量なクライアント側パッチです。

### インストール方法

`ysm-equipment-render-patch-<version>.jar`をダウンロードし、クライアント側の`mods/`フォルダに入れてください。Yes Steve Model がインストールされていることを確認してください。Curios スロットのエリトラやランタンを使用する場合のみ、Curios と対応する装備スロット追加 mod も導入してください。

### ビルド方法

1. JDK 21 を導入します。
2. このリポジトリを clone またはダウンロードします。
3. 以下のコマンドを実行します。

```bash
./gradlew build
```

ビルドされた jar は `build/libs/` に生成されます。

### クレジット

ランタンの揺れ挙動は [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns) の揺れ方を利用しています。