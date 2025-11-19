# GitHub Copilot Instructions for Food Ordering System

このプロジェクトは、マイクロサービスアーキテクチャとドメイン駆動設計（DDD）に基づく食品注文システムです。

## プロジェクト構造

このプロジェクトは以下のマイクロサービスで構成されています：
- **order-service**: 注文管理サービス
- **payment-service**: 決済管理サービス
- **restaurant-service**: レストラン管理・注文承認サービス
- **customer-service**: 顧客管理サービス
- **common**: 共通モジュール
- **infrastructure**: インフラ設定（Kafka、PostgreSQL等）

各サービスは以下のモジュール構造を持ちます：
- `{service}-domain-core`: ドメインエンティティ、値オブジェクト、ドメインサービス
- `{service}-application-service`: アプリケーションサービス、ポート、DTO
- `{service}-dataaccess`: JPAエンティティ、リポジトリ、アダプター
- `{service}-messaging`: Kafkaメッセージング
- `{service}-container`: Spring Bootアプリケーション、設定

## コーディング規約

### クラス内の要素の並び順
1. フィールド
2. コンストラクタ
3. メソッド
   - Getter/Setter（フィールドの並び順と同一）
   - publicメソッド
   - privateメソッド（呼び出される順）
4. 内部クラス（Builderクラス等）

### this の使用
- **必須**: インスタンス変数へのアクセスには`this.`を明示的に使用すること
- 理由: ローカル変数・引数との区別、可読性向上

```java
// 良い例
public void setPrice(Money price) {
    this.price = price;
}

// 悪い例
public void setPrice(Money price) {
    price = price;  // NG
}
```

### Javadocコメント
- **必須**: すべてのクラス、インターフェース、列挙型にJavadocコメントを記述すること
- **必須**: すべてのpublicメソッドにJavadocコメントを記述すること（引数、戻り値を含む）
- **推奨**: DTOクラスのフィールドにはインラインJavadocコメント（`/** コメント */`）を記述
- **推奨**: 列挙型の各値にインラインJavadocコメントを記述

```java
/**
 * 注文承認リクエストを処理する入力ポート
 */
public interface RestaurantApprovalRequestMessageListener {

    /**
     * 注文の承認処理を実行
     * @param restaurantApprovalRequest 注文承認リクエスト
     */
    void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}

/**
 * Order Serviceからの注文承認リクエストを表すDTO
 */
@Getter
@Builder
@AllArgsConstructor
public class RestaurantApprovalRequest {

    /** イベントID */
    private String id;
    /** Sagaトランザクション管理ID */
    private String sagaId;
    /** レストランID */
    private String restaurantId;
}
```

### Builderパターン
エンティティやDTOクラスには、静的内部クラスとしてBuilderパターンを実装してください。

```java
public class Payment extends AggregateRoot<PaymentId> {
    private final OrderId orderId;
    private final CustomerId customerId;
    
    private Payment(Builder builder) {
        super.setId(builder.paymentId);
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private PaymentId paymentId;
        private OrderId orderId;
        private CustomerId customerId;
        
        public Builder paymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }
        
        public Builder orderId(OrderId orderId) {
            this.orderId = orderId;
            return this;
        }
        
        public Payment build() {
            return new Payment(this);
        }
    }
}
```

## DDDアーキテクチャパターン

### エンティティ
- `AggregateRoot<ID>` または `BaseEntity<ID>` を継承
- イミュータブルなフィールドは`final`で宣言
- ビジネスロジックはエンティティ内に実装

### 値オブジェクト
- `BaseId<T>` を継承してID型を定義
- イミュータブル（すべてのフィールドが`final`）

### ドメインサービス
- 複数のエンティティにまたがるビジネスロジックを実装
- インターフェースと実装クラスに分離
- Springの依存関係を含まない（pure Java）

### イベント
- `DomainEvent<T>` インターフェースを実装
- イミュータブル

## 命名規約

### パッケージ構造
```
com.food.ordering.system.{service}.service.domain
├── entity/          # エンティティ
├── valueobject/     # 値オブジェクト
├── event/           # ドメインイベント
├── exception/       # ドメイン例外
└── {Service}DomainService.java
```

### クラス命名
- エンティティ: `Payment`, `Order`, `Restaurant`
- 値オブジェクト: `PaymentId`, `OrderId`, `RestaurantId`
- イベント: `PaymentCompletedEvent`, `OrderApprovedEvent`
- 例外: `PaymentDomainException`, `RestaurantNotFoundException`
- サービス: `PaymentDomainService`, `PaymentDomainServiceImpl`

## Kafkaメッセージング

### トピック命名
- `{source}-{action}-{topic}`
- 例: `payment-request`, `payment-response`, `restaurant-approval-request`

### メッセージフロー
1. Order Service → Kafka → Payment Service
2. Payment Service → Kafka → Order Service
3. Order Service → Kafka → Restaurant Service
4. Restaurant Service → Kafka → Order Service

## データベース

### スキーマ
- 各サービスは独自のスキーマを持つ
- スキーマ名: `order`, `payment`, `restaurant`, `customer`

### テーブル命名
- 小文字、アンダースコア区切り
- 例: `payments`, `credit_entry`, `credit_history`

### 初期化スクリプト
- `init-schema.sql`: テーブル定義、ENUM型
- `init-data.sql`: テストデータ

## 依存関係の原則

### ドメイン層（domain-core）
- **依存可能**: common-domainのみ
- **依存禁止**: Spring Framework、Jakarta Persistence等

### アプリケーション層（application-service）
- **依存可能**: domain-core, common-domain, spring-boot-starter-validation, spring-tx
- ポート（インターフェース）を定義

### データアクセス層（dataaccess）
- **依存可能**: application-service, spring-boot-starter-data-jpa, postgresql
- ポートの実装（アダプター）

### メッセージング層（messaging）
- **依存可能**: application-service, kafka-producer, kafka-consumer, kafka-model

## ロギング

- Lombokの`@Slf4j`を使用
- ログレベル: DEBUG（開発時）

## 技術スタック

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Apache Avro
- Lombok
- Maven

## テスト用データ

### 顧客ID
- Customer 1: `d215b5f8-0249-4dc5-89a3-51fd148cfb41`
- Customer 2: `d215b5f8-0249-4dc5-89a3-51fd148cfb42`

### レストランID
- Restaurant 1: レストランIDを適切に設定
- Restaurant 2: レストランIDを適切に設定

### 商品
- 各レストランに複数の商品を設定
- 価格と在庫状態を含む

## コード生成時の注意点

1. **パターンの一貫性**: 既存のサービス（order-service, payment-service）のパターンを踏襲
2. **ドメインロジックの配置**: ビジネスルールはドメイン層に実装
3. **イミュータビリティ**: エンティティの重要なフィールドは`final`で宣言
4. **例外処理**: ドメイン例外を適切に使用
5. **ログ出力**: 重要な処理の開始・終了・エラー時にログを出力
6. **タスク管理**: タスク完了時は`_Todo/`フォルダー内の該当タスクリストファイルを更新すること
   - 進捗状況（完了数/総数）を更新
   - タスクのステータスを「✅ 完了」に変更
   - 作成したファイルや実装内容を記録

## 参考資料

プロジェクトの設計ドキュメントは`_Design/`フォルダーにあります：
- `CodingRule.md`: 詳細なコーディングルール
- `payment-service_design.md`: Payment Serviceの設計
- `uml/`: UML図
