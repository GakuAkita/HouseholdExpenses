# Mail fixtures

Synthetic emails written to match the current parser regexes. They are not copies of real emails.
Replace them with real (anonymized) emails when available, and update the expected values in
`test/unit/mailParsers.test.ts`.

`rakuten_card_all*.txt` follow the layout of real 楽天カード「カード利用のお知らせ」 mails (2026/10):
the text layout (`rakuten_card_all`), and the HTML layout as Gmail renders it
(`rakuten_card_all_html`)
and with the tags removed (`rakuten_card_all_html_stripped`, kept with CRLF). Every store, amount
and date in them is invented. This repository is public: never put values from a real mail here.
