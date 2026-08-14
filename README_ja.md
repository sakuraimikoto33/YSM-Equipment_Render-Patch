# YSM Equipment Render Patch

[English](README.md) | **日本語**

YSM Equipment Render Patch は、[Yes Steve Model](https://www.curseforge.com/minecraft/mc-mods/yes-steve-model) を使用するプレイヤーに対応装備を正しく描画するためのクライアント側互換 Mod です。

## 機能

- YSM プレイヤーにエリトラ機能を持つチェスト装備を描画します。
- Draconic Evolution 導入時、フライトモジュールのエリトラ設定に対応します。
- Curios スロットに装備した対応ランタンを揺れアニメーション付きで描画します。
- Curios の head スロットに装備した Kaleidoscope Doll を描画します。
- Curios スロットの表示設定に従います。
- 非公開マッピングを同梱せず、YSM Mapping API を使用して実行時の YSM 互換性を提供します。

装備登録や Curios スロットへ装備可能にする仕様は、対応する装備 Mod 側が担当します。本プロジェクトは描画連携を提供します。

## 対応バージョン

| Minecraft | Mod ローダー | Yes Steve Model | Curios | Draconic Evolution | Kaleidoscope Doll | Java |
| --- | --- | --- | --- | --- | --- | --- |
| 1.20.1 | Forge 47.4.20+ | 2.6.0-forge+mc1.20.1+ | 5.14.1+1.20.1+ | 3.1.2.621+ | 1.4.1-forge+mc1.20.1+ | 17 |
| 1.21.1 | NeoForge 21.1.228+ | 2.6.0-neoforge+mc1.21.1+ | 9.5.1+1.21.1+ | 3.1.4.632+ | 1.4.1-neoforge+mc1.21.1+ | 21 |

対応するソースブランチは、それぞれ `mc/1.20.1` と `mc/1.21.1` です。

`main` ブランチには、リポジトリ共通のドキュメントとメンテナンスポリシーを配置しています。ビルド可能な Mod のソースは、対応する `mc/<minecraft-version>` ブランチで管理します。

## 必須 Mod

常に必要:

- Yes Steve Model
- YSM Mapping API

任意の連携 Mod:

- Curios API
- Draconic Evolution
- Kaleidoscope Doll

YSM Mapping API 0.1.0 以降が必須です。Curios、Draconic Evolution、Kaleidoscope Doll は任意で、最低バージョンは上表に記載しています。

## 導入方法

1. Minecraft バージョンと Mod ローダーに対応する jar をダウンロードします。
2. Yes Steve Model と YSM Mapping API を導入します。
3. 必要な jar をクライアント側の `mods/` フォルダーへ配置します。
4. 使用する機能に応じて任意の連携 Mod を導入します。

本 Mod はクライアント側専用です。

## ビルド方法

対象 Minecraft バージョンのブランチをチェックアウトし、上記の Java バージョンと Git を導入して次のコマンドを実行します。通常ビルドでは公開済み YSM Mapping API タグを毎回確認し、このブランチと Minecraft バージョンが完全一致する `0.1.0` 以上の最新安定版を選択します。`ysm_mapping_api_version` はタグ選択、`ysm_mapping_api_version_range` はLoader依存下限に使用し、両方に同じ安定版SemVerを指定します。

```bash
./gradlew build
```

Windows では `gradlew.bat build` を使用します。生成された jar は `build/libs/` に出力されます。

ローカルの YSM Mapping API checkout を使う場合は `-PysmMappingApiPath=<checkout>` を指定します。設定時に Minecraft バージョンと安定 API 版を検証します。オフラインビルドでは、このローカル上書きが必須です。

## クレジット

ランタンの揺れ挙動は [Toni's Immersive Lanterns](https://modrinth.com/mod/immersive-lanterns) を参考にしています。

## ライセンス

本プロジェクトは MIT License の下で提供されます。
