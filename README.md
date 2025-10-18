# Household Expenses App

**家計簿アプリ** - メールから自動で支出を記録し、手間のかからない家計管理を実現

## ✨ 何がすごいのか？

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