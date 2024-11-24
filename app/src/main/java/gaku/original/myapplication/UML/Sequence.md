```mermaid
sequenceDiagram

User ->> UI: 金額を入力
User ->> UI: Addボタンを押す
UI ->> Local_DB: DBを更新
Local_DB ->> Remote_DB:a

````