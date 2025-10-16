export class AmazonSubscribeCancelParser {
  constructor(private rawText: string) {}

  /**
   * 
    秋田岳様、こんにちは
    お客様の定期購入はキャンセルされました。おすすめ商品はこちら。

    定期購入はキャンセルされました。

    アイリスオーヤマ(IRIS OHYAMA) 天然水 ラベルレス 500ml ×2... (/%E3%82%A2%E3%82%A4%E3%83%AA%E3%82%B9%E3%82%AA%E3%83%BC%E3%83%A4%E3%83%9E-%E3%83%A9%E3%83%99%E3%83%AB%E3%83%AC%E3%82%B9-%E5%AF%8C%E5%A3%AB%E5%B1%B1%E3%81%AE%E5%A4%A9%E7%84%B6%E6%B0%B4-500ml-%C3%9724%E6%9C%AC/dp/B09LCPT9DQ)

    アイリスオーヤマ(IRIS OHYAMA) 天然水 ラベルレス 500ml ×2...

    この商品を配送スケジュールを変更したいですか？定期おトク便商品はいつでも再設定や編集ができます。

    定期おトク便に再度登録する (https://www.amazon.co.jp/auto-deliveries/subscription?sourcePage=email&subscriptionId=SNST0_6FB1D35CA258441CBCED&listFilter=inactive&ref_=sns_em_can_re)


    おすすめの代替品

    アイリスオーヤマ 炭酸水 ラベルレス 500ml ×24本 富士山の強炭酸水... (https://www.amazon.co.jp/auto-deliveries/switchProductConfirmation?sourcePage=email&subscriptionId=SNST0_6FB1D35CA258441CBCED&ASIN=B09LCRNQT4&ref_=sns_em_can_sp_cn_rio)

    アイリスオーヤマ 炭酸水 ラベルレス 500ml ×24本 富士山の強炭酸水...

    他のおすすめ商品を見る (https://www.amazon.co.jp/auto-deliveries/recommendations?subscriptionId=SNST0_6FB1D35CA258441CBCED&ASIN=B09LCPT9DQ&ref_=sns_em_can_see_rep&snsActionCompleted=&sourcePage=email)

   */
  extractProductName(): string | null {
    const regexAll =
      /([^\r\n]+?)\s*(?:\.{3}|…)?\s*\r?\n\s*この商品を配送スケジュールを変更したいですか？/g;

    const matches = [...this.rawText.matchAll(regexAll)].map(
      (m) => m[1].trim() // 行末の...がなくてもそのまま取得
    );

    return matches.length > 0 ? matches[0] : null;
  }
}
