# Curios Yes Steve Model Render Patch

- [English](#english)
- [日本語](#japanese)

<a id="english"></a>
## English

### Overview

Curios Yes Steve Model Render Patch is a small client-side compatibility patch that lets equipment worn through Curios appear correctly when using Yes Steve Model.

It currently supports rendering Elytra and Lantern equipped in Curios slots.

### Requirements

| What | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228+ |
| Curios | 9.5.1+1.21.1+ |
| Yes Steve Model | 2.6.5-neoforge+mc1.21.1 |

### Features

- Displays Elytra equipped through Curios on the active YSM model.
- Displays Lantern equipped through Curios on the active YSM model.
- Applies simple swing physics to rendered lanterns.
- Works as a lightweight client-side render patch without adding new gameplay mechanics.

### Credits

The lantern swing behavior is based on [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns).

### Installation

Download `curios-ysm-render-patch-<version>.jar` and place it in the client-side `mods/` folder. Make sure Curios and Yes Steve Model are also installed.

### Building

1. Install JDK 21.
2. Clone or download this repository.
3. Run the build command:

```bash
./gradlew build
```

The built jar will be generated in `build/libs/`.

<a id="japanese"></a>
## 日本語

### 概要

Curios Yes Steve Model Render Patch は、Yes Steve Model 使用時に Curios で装備している一部装備を正しく表示するための、小さなクライアント側互換パッチです。

現在は、Curios に装備したエリトラとランタンの描画に対応しています。

### 要件

| 項目 | バージョン |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228+ |
| Curios | 9.5.1+1.21.1+ |
| Yes Steve Model | 2.6.5-neoforge+mc1.21.1 |

### 特徴

- Curios に装備した エリトラ を使用中の YSM モデル上に表示します。
- Curios に装備した ランタン を使用中の YSM モデル上に表示します。
- 表示されたランタンに簡易的な揺れを適用します。
- 新しいゲーム要素を追加せず、描画だけを補正する軽量なクライアント側パッチです。

### クレジット

ランタンの揺れ挙動は [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns) の揺れ方を利用しています。

### インストール方法

`curios-ysm-render-patch-<version>.jar`をダウンロードし、クライアント側の`mods/`フォルダに入れてください。Curios と Yes Steve Model もインストールされていることを確認してください。

### ビルド方法

1. JDK 21 を導入します。
2. このリポジトリを clone またはダウンロードします。
3. 以下のコマンドを実行します。

```bash
./gradlew build
```

ビルドされた jar は `build/libs/` に生成されます。