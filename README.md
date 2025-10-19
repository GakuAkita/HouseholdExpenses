# 家計簿 for エンジニア
# Household Expenses App for Engineers

## なぜ作ったのか
市販の家計簿を使っている限りはそれに新しい機能を加えたり無駄な機能を削ぎ落としたりということはできない。本をKindleでよく購入するが、毎回購入後にメールが届くのでそれをもとに自動で家計簿に書いてくれないかとずっと思っていた。そこで自分で作ることにした。Kindleに限らず、メールが届きさえすればどのようなものでも抽出できるので、電気代の通知メールや楽天Payのメールにも対応している。メールのフォーマットが変わってしまえば読み取り関数も変更する必要があるので、適宜アップデートしていく。

### 🤖 **完全自動化された支出記録**
- **Amazon購入** → 注文確認メールから自動で支出を抽出・記録
- **楽天ペイ決済** → 利用確認メールから店舗名・金額を自動抽出
- **定期購入** → Amazon定期便の発送通知から自動記録
- **各種サービス** → Kindle、Udemy、四国電力など多様なサービスに対応。メールから情報を抜き出し、自動で記録

### 📱 **モダンなAndroidアプリ**
- **Jetpack Compose**による美しいUI
- **カレンダー表示**で支出を視覚的に管理
- **リアルタイム同期**で複数端末間でデータ共有

## 🏗️ 技術スタック

### フロントエンド (Android)
- **Kotlin** + **Jetpack Compose**
- **Room Database** (ローカルストレージ)
- **Firebase** (認証・クラウド同期)
- **Hilt** (依存性注入)
- **OCR** (レシート読み取り)

### バックエンド (Firebase Functions)
- **TypeScript** + **Node.js**
- **Gmail API** (メール自動処理)
- **Firestore** (データベース)
- **Realtime Database** (設定管理)

# Lisence

This project is licensed under the MIT License, see the LICENSE.txt file for details