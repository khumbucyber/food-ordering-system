# 本ファイルについて

プロジェクトでコードの記述に一貫性を持たせるためのコーディングルールを定義する。

# ルール
## クラス内のフィールド・メソッドの並び順
以下の並び順とする。
- フィールド
- コンストラクタ
- メソッド
    - Getter/Setter
        - フィールドの並び順と同一順とする
    - publicメソッド
    - privateメソッド
        - 概ね、クラス内の他のメソッドから呼び出される順とする
- 内部クラス（Builderパターンを記述する場合のBuilderクラス など）

## thisの明示的な使用
- インスタンス変数には`this.`を明示してアクセスすること。
    - インスタンス変数とローカル変数・引数の名前が重複の区別のため（必須）
    - 可読性を高めるため

    public PaymentEvent validateAndCancelPayment(Payment payment,
                                                 CreditEntry creditEntry,
                                                 List<CreditHistory> creditHistories,
                                                 List<String> failureMessages) {