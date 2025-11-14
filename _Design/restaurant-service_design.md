# restaurant-service の設計

## 1. 概要

### 1.1 レストランドメインの役割
レストランサービスは、パートナーレストランが受注した注文の内容を検証し、承認または拒否を行うマイクロサービスです。
注文内容の整合性チェック（商品の存在、価格の一致、支払いステータス）を担当し、承認結果をOrder Serviceに返却します。

### 1.2 ビジネスフロー
1. Order Service から注文承認リクエストを受信（Kafkaメッセージ）
2. レストランの商品マスタと注文内容を照合
   - 商品の存在確認
   - 商品名の一致確認
   - 商品価格の一致確認
   - 注文ステータスが PAID であることの確認
3. 検証結果に基づいて承認または拒否を決定
4. Order Service に承認結果を通知（Kafkaメッセージ）
5. Order Service が注文ステータスを更新（APPROVED または CANCELLING）

## 2. モジュールの構成

```
restaurant-service
├── restaurant-container          # Spring Boot アプリケーション、設定
├── restaurant-dataaccess         # 出力アダプター: JPA エンティティ、リポジトリ実装
├── restaurant-domain
│   ├── restaurant-domain-core           # ドメインエンティティ、値オブジェクト、ドメインサービス
│   └── restaurant-application-service   # ポート定義、ポート実装、Helper、DTO
└── restaurant-messaging          # 入力アダプター: Kafka リスナー、Avro変換
```

### 2.1 ヘキサゴナルアーキテクチャの役割分担

このプロジェクトでは、ヘキサゴナルアーキテクチャ（ポート＆アダプター）パターンを実践的に実装しています。

#### 理論と実装の対応

**理論的な配置**:
- **ポート（インターフェース）**: アプリケーション層に定義
- **アダプター（実装）**: インフラストラクチャ層に配置

**このプロジェクトの実装**:
- **入出力ポート（インターフェース）**: `restaurant-application-service`に定義
- **入力ポート（実装実装）（`*MessageListenerImpl`）**: `restaurant-application-service`に配置
- **入力アダプター（`*KafkaListener`）**: `restaurant-messaging`に配置
- **出力アダプター（`*RepositoryImpl`）**: `restaurant-dataaccess`に配置

#### 2層の入力アダプター構造

```
[Kafka] 
   ↓
┌─────────────────────────────────────────────────────────┐
│ restaurant-messaging (入力アダプター層)                 │
│  RestaurantApprovalRequestKafkaListener                 │
│    - @KafkaListener でメッセージ受信                    │
│    - Avroモデル → DTO 変換                              │
│    - Kafka固有の処理（デシリアライズ、エラーハンドリング）│
└─────────────────────────────────────────────────────────┘
   ↓ (messageListener.approveOrder(dto) を呼び出し)
┌─────────────────────────────────────────────────────────┐
│ restaurant-application-service (ポート実装層)           │
│  RestaurantApprovalRequestMessageListenerImpl           │
│    - @Service でビジネスロジック実行                    │
│    - Kafkaに依存しない（テスト容易性）                  │
│    - Helper → DomainService → Repository 呼び出し       │
└─────────────────────────────────────────────────────────┘
   ↓
[restaurant-domain-core] → [restaurant-dataaccess] → [Database]
```

#### なぜImplがapplication-serviceにあるのか？

1. **技術独立性**: ビジネスロジックをKafkaから分離
   - `*MessageListenerImpl`: Kafkaを知らない（Avroモデル不使用）
   - `*KafkaListener`: ビジネスロジックを知らない（DTO変換のみ）

2. **テスト容易性**: 
   - `*MessageListenerImpl`: 単純なJUnitテストで検証可能（Kafka不要）
   - `*KafkaListener`: Kafka Testcontainersで結合テスト

3. **再利用性**: 
   - REST APIを追加する場合、同じ`*MessageListenerImpl`を再利用可能
   - メッセージング技術を変更（例: RabbitMQ）しても、`*MessageListenerImpl`は無変更

4. **パターンの一貫性**:
   - Payment Service、Order Serviceでも同じ構造
   - PaymentRequestMessageListenerImpl、PaymentResponseMessageListenerImpl など

#### モジュール責務のまとめ

| モジュール | 役割 | 依存関係 | 代表的なクラス |
|-----------|------|---------|---------------|
| `restaurant-domain-core` | ドメインロジック（純粋Java） | common-domain のみ | Restaurant, OrderApproval, RestaurantDomainServiceImpl |
| `restaurant-application-service` | ポート定義 + ポート実装 | domain-core, Spring（DI, Tx） | RestaurantApprovalRequestMessageListenerImpl, RestaurantApprovalRequestHelper |
| `restaurant-messaging` | Kafka入力アダプター | application-service, Kafka, Avro | RestaurantApprovalRequestKafkaListener |
| `restaurant-dataaccess` | 出力アダプター（永続化） | application-service, JPA, PostgreSQL | RestaurantRepositoryImpl, OrderApprovalRepositoryImpl |
| `restaurant-container` | アプリケーション構成 | すべてのモジュール | RestaurantServiceApplication, BeanConfiguration |

## 3. パッケージの構成

### 3.1 restaurant-domain-core module
```
com.food.ordering.system.restaurant.service.domain
├── entity/          # エンティティ
├── valueobject/     # 値オブジェクト
├── event/           # ドメインイベント
├── exception/       # ドメイン例外
├── RestaurantDomainService.java        # ドメインサービス（インターフェース）
└── RestaurantDomainServiceImpl.java    # ドメインサービス（実装）
```

### 3.2 restaurant-application-service module
```
com.food.ordering.system.restaurant.service.domain
├── dto/             # データ転送オブジェクト
│   ├── RestaurantApprovalRequest.java
│   └── ...
├── ports/
│   ├── input/
│   │   └── message/
│   │       └── listener/
│   │           ├── RestaurantApprovalRequestMessageListener.java      # インターフェース
│   │           └── RestaurantApprovalRequestMessageListenerImpl.java  # 実装（@Service）
│   └── output/
│       ├── repository/
│       │   ├── RestaurantRepository.java              # インターフェース
│       │   └── OrderApprovalRepository.java           # インターフェース
│       └── message/
│           └── publisher/
│               └── RestaurantApprovalResponseMessagePublisher.java  # インターフェース
├── mapper/          # DTO ↔ Entity マッパー
│   └── RestaurantDataMapper.java
└── RestaurantApprovalRequestHelper.java  # ビジネスロジック調整（@Component、@Transactional）
```

**重要**: 
- `*MessageListenerImpl`クラスは`restaurant-application-service`モジュールに配置
- `@Service`アノテーションを持ち、Springのビジネスロジック層として機能
- Kafkaに依存せず、DTOのみを扱う（技術独立性）
- `restaurant-messaging`の`*KafkaListener`から呼び出される

## 4. エンティティの設計

### 4.1 Restaurant エンティティ（集約ルート）
* **継承**: `AggregateRoot<RestaurantId>`
* **フィールド**:
  * `RestaurantId id` - レストランID（AggregateRootから継承）
  * `OrderApproval orderApproval` - 注文承認結果
  * `boolean active` - レストランのアクティブ状態
  * `OrderDetail orderDetail` - 承認対象の注文詳細（final）

* **メソッド**:
  * `void validateOrder(List<String> failureMessages)` - 注文内容の検証
    - 注文ステータスが PAID であることを確認
    - 注文の各商品について以下を検証:
      - 商品がレストランに存在すること
      - 商品名が一致すること
      - 商品価格が一致すること
    - 検証失敗時は failureMessages に追加
  * `void constructOrderApproval(OrderApprovalStatus orderApprovalStatus)` - OrderApproval エンティティを構築
  * `void setActive(boolean active)` - アクティブ状態の設定
  * `OrderApproval getOrderApproval()` - 承認結果の取得
  * `boolean isActive()` - アクティブ状態の確認
  * `OrderDetail getOrderDetail()` - 注文詳細の取得

* **設計意図**:
  - レストラン自体のマスタ情報は保持せず、承認審査に特化
  - 注文スナップショットを取り込み、レストラン視点で整合性チェック
  - 承認ワークフローを集約内で完結

* **命名の補足**:
  - **概念上の名称**: `RestaurantOrderRequest`（レストランへの注文リクエスト）
  - **実装上のクラス名**: `Restaurant`
  - **命名の理由**: Order Serviceからの注文承認リクエストをレストラン側で受け取り、商品マスタと照合して審査する集約ルート。レストランマスタ情報そのものではなく、「レストランに対する注文リクエストの審査プロセス」を表現する。
  - **OrderApprovalとの区別**:
    - `Restaurant`: リクエスト全体（注文情報 + 商品マスタ参照 + 検証 + 承認結果生成）
    - `OrderApproval`: 承認結果のみ（APPROVED/REJECTED）
  - **activeフィールド**: レストランマスタ（`restaurant.restaurants`テーブル）から取得した営業状態のスナップショット。休業中（active=false）のレストランへのリクエストは即座に拒否される。

* **インスタンス生成**: Builderパターン

### 4.2 OrderApproval エンティティ
* **継承**: `BaseEntity<OrderApprovalId>`
* **フィールド**:
  * `OrderApprovalId id` - 承認ID（BaseEntityから継承）
  * `RestaurantId restaurantId` - レストランID（final）
  * `OrderId orderId` - 注文ID（final）
  * `OrderApprovalStatus approvalStatus` - 承認ステータス（final）

* **メソッド**:
  * Getter メソッドのみ（イミュータブル）

* **設計意図**:
  - レストランによる注文承認結果を表す
  - イミュータブルな設計で一度確定した承認結果の変更を防止

* **インスタンス生成**: Builderパターン

### 4.3 OrderDetail エンティティ
* **継承**: `BaseEntity<OrderId>`
* **フィールド**:
  * `OrderId id` - 注文ID（BaseEntityから継承）
  * `OrderStatus orderStatus` - 注文ステータス
  * `Money totalAmount` - 注文合計金額
  * `List<Product> products` - 注文商品リスト（final）

* **メソッド**:
  * Getter メソッド

* **設計意図**:
  - Order Serviceから受信した注文情報のスナップショット
  - 承認審査のためにレストラン集約内で保持

* **インスタンス生成**: Builderパターン

### 4.4 Product エンティティ
* **継承**: `BaseEntity<ProductId>`
* **フィールド**:
  * `ProductId id` - 商品ID（BaseEntityから継承）
  * `String name` - 商品名
  * `Money price` - 商品価格
  * `int quantity` - 数量
  * `boolean available` - 利用可能フラグ

* **メソッド**:
  * `void updateWithConfirmedNamePriceAndAvailability(String name, Money price, boolean available)` - 確認済み情報で更新
  * Getter メソッド

* **設計意図**:
  - 注文商品とレストラン商品マスタの両方を表現
  - 照合時にレストランマスタの情報で更新可能

* **インスタンス生成**: Builderパターン

## 5. 値オブジェクトの設計

### 5.1 OrderApprovalId
* `BaseId<UUID>` を継承
* 型: `UUID`
* 説明: 注文承認を一意に識別するID（注文IDと同じ値を使用）

### 5.2 OrderApprovalStatus（列挙型）
* **パッケージ**: `com.food.ordering.system.domain.valueobject`（common-domain）
* **値**:
  * `APPROVED` - 承認済み
  * `REJECTED` - 拒否

* **説明**: 
  - 注文承認の最終結果を表す列挙型
  - common-domainで定義され、複数のサービス（Order Service、Restaurant Service）で共有
  - イミュータブルで型安全な承認ステータス表現

* **Kafka連携**:
  - Avroスキーマでも同じ値を定義（`kafka-model`モジュール）
  - ドメイン値オブジェクト ↔ Avroモデル間で変換して使用

## 6. ドメインイベントの設計

### 6.1 OrderApprovalEvent（抽象クラス）
* **実装**: `DomainEvent<OrderApproval>`
* **フィールド**:
  * `OrderApproval orderApproval` - 承認結果
  * `RestaurantId restaurantId` - レストランID
  * `List<String> failureMessages` - 失敗メッセージリスト
  * `ZonedDateTime createdAt` - イベント作成日時

* **メソッド**:
  * Getter メソッド

* **設計意図**: 承認イベントの共通基底クラス

### 6.2 OrderApprovedEvent
* **継承**: `OrderApprovalEvent`
* **説明**: 注文が承認された際に発行されるイベント
* **ペイロード**: orderApproval, restaurantId, failureMessages (空), createdAt

### 6.3 OrderRejectedEvent
* **継承**: `OrderApprovalEvent`
* **説明**: 注文が拒否された際に発行されるイベント
* **ペイロード**: orderApproval, restaurantId, failureMessages (検証エラー), createdAt

すべてのイベントはイミュータブルです。

### 6.4 restaurantIdフィールドの整合性について

**補足説明**: `OrderApprovalEvent`には`restaurantId`フィールドと`orderApproval.restaurantId`の両方が存在しますが、これらは常に同じ値を持ちます。

* **整合性の保証**:
  ```java
  // Restaurant.constructOrderApproval()内で整合性を保証
  this.orderApproval = OrderApproval.builder()
      .restaurantId(getId())  // ← Restaurant集約の自分のIDを設定
      .orderId(orderDetail.getId())
      .approvalStatus(orderApprovalStatus)
      .build();
  ```

* **冗長性の設計意図**:
  1. **イベントの自己完結性**: イベント受信側が`OrderApproval`オブジェクトをネストして辿らなくても、トップレベルで直接`restaurantId`を参照可能
  2. **パフォーマンス**: Kafka送信時やログ出力時に、ネストしたオブジェクトを開かずに識別子にアクセス
  3. **可読性**: イベントの主要な識別子（orderId, restaurantId, sagaId）をトップレベルで明示
  4. **Avro変換の効率化**: フラットな構造でAvroスキーマとのマッピングが容易

* **使用例**:
  ```java
  // RestaurantDomainServiceImpl.validateOrder()
  return new OrderApprovedEvent(
      restaurant.getOrderApproval(),  // restaurantId を内包
      restaurant.getId(),              // 同じ restaurantId（整合性保証済み）
      failureMessages,
      ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)),
      orderApprovedEventPublisher
  );
  ```

## 7. ドメインサービスの設計

### 7.1 RestaurantDomainService（インターフェース）
```java
public interface RestaurantDomainService {
    OrderApprovalEvent validateOrder(
        Restaurant restaurant,
        List<String> failureMessages,
        DomainEventPublisher<OrderApprovedEvent> orderApprovedEventPublisher,
        DomainEventPublisher<OrderRejectedEvent> orderRejectedEventPublisher
    );
}
```

### 7.2 RestaurantDomainServiceImpl（実装）
* **ビジネスロジック**:
  1. `restaurant.validateOrder(failureMessages)` を呼び出し、注文内容を検証
  2. failureMessages が空の場合:
     - 承認ステータスを APPROVED に設定
     - OrderApprovedEvent を生成して返却
  3. failureMessages にエラーがある場合:
     - 承認ステータスを REJECTED に設定
     - OrderRejectedEvent を生成して返却

* **設計意図**:
  - 複数エンティティ（Restaurant, OrderApproval）にまたがるビジネスロジック
  - ドメインイベントの生成と適切なパブリッシャーへの関連付け
  - Springの依存関係を含まない純粋なJavaクラス

## 8. ドメイン例外の設計

### 8.1 RestaurantDomainException
* **説明**: Restaurant ドメインの基底例外クラス
* **継承**: `DomainException`
* **用途**: レストランサービス固有のビジネスルール違反

### 8.2 RestaurantNotFoundException
* **説明**: レストランが見つからない場合の例外
* **継承**: `DomainException`
* **用途**: 指定されたRestaurantIdに対応するレストランが存在しない

## 9. アプリケーションサービスの設計

### 9.1 ポート（入力）

#### RestaurantApprovalRequestMessageListener（インターフェース）
```java
public interface RestaurantApprovalRequestMessageListener {
    void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}
```
* **説明**: Order Serviceからの注文承認リクエストを処理するポート
* **配置**: `restaurant-application-service`モジュール
* **実装クラス**: `RestaurantApprovalRequestMessageListenerImpl`

#### RestaurantApprovalRequestMessageListenerImpl（実装）
```java
@Slf4j
@Service
public class RestaurantApprovalRequestMessageListenerImpl 
    implements RestaurantApprovalRequestMessageListener {
    
    private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;
    
    @Override
    public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        log.info("Processing restaurant approval for order id: {}", 
                 restaurantApprovalRequest.getOrderId());
        restaurantApprovalRequestHelper.persistOrderApproval(restaurantApprovalRequest);
    }
}
```

* **配置**: `restaurant-application-service`モジュール（`restaurant-messaging`ではない）
* **アノテーション**: `@Service`（Springのビジネスロジック層）
* **責務**:
  - 注文承認リクエストの受付
  - `RestaurantApprovalRequestHelper`へのデリゲート
  - ビジネスロジックの調整（トランザクション境界の外側）
* **技術独立性**:
  - Kafkaに依存しない（`*AvroModel`を使用しない）
  - DTOのみを扱う
  - 単純なJUnitテストで検証可能
* **呼び出し元**: `RestaurantApprovalRequestKafkaListener`（`restaurant-messaging`モジュール）

**設計意図**:
- **ヘキサゴナルアーキテクチャの実装**: ポートの実装をapplication-service層に配置することで、ビジネスロジックをインフラストラクチャから分離
- **テスト容易性**: Kafka環境なしでビジネスロジックをテスト可能
- **再利用性**: REST APIなど別の入力経路を追加する場合、同じ実装を再利用可能

### 9.2 ポート（出力）

#### RestaurantRepository
```java
public interface RestaurantRepository {
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
```
* **説明**: レストラン情報（商品マスタ）の取得
* **引数**: Restaurant（restaurantIdと商品IDリストを含む）
* **戻り値**: レストラン情報と商品詳細を含むRestaurant
* **実装**: `RestaurantRepositoryImpl`（restaurant-dataaccess）

#### OrderApprovalRepository
```java
public interface OrderApprovalRepository {
    OrderApproval save(OrderApproval orderApproval);
}
```
* **説明**: 承認結果の永続化
* **実装**: `OrderApprovalRepositoryImpl`（restaurant-dataaccess）

#### RestaurantApprovalResponseMessagePublisher
```java
public interface RestaurantApprovalResponseMessagePublisher {
    void publish(OrderApprovalEvent orderApprovalEvent);
}
```
* **説明**: 承認結果のKafka送信
* **実装**: `RestaurantApprovalResponseKafkaPublisher`（restaurant-messaging）

### 9.3 DTO設計

#### RestaurantApprovalRequest
```java
public class RestaurantApprovalRequest {
    private String id;              // イベントID
    private String sagaId;          // Sagaトランザクション管理ID
    private String restaurantId;    // レストランID
    private String orderId;         // 注文ID
    private RestaurantOrderStatus restaurantOrderStatus;  // 注文ステータス
    private List<Product> products; // 注文商品リスト
    private BigDecimal price;       // 合計金額
    private Instant createdAt;      // 作成日時
}
```

#### Product（DTO）
```java
public class Product {
    private String id;          // 商品ID
    private Integer quantity;   // 数量
}
```

#### RestaurantOrderStatus（列挙型）
```java
public enum RestaurantOrderStatus {
    PAID  // 支払い済み（承認対象）
}
```

### 9.4 ビジネスロジッククラス

#### RestaurantApprovalRequestHelper
* **責務**:
  - DTOからドメインエンティティへの変換調整
  - ドメインサービスの呼び出し
  - リポジトリとメッセージパブリッシャーの調整
  - トランザクション管理（@Transactional）

* **主要メソッド**:
  ```java
  @Transactional
  public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest);
  ```

* **処理フロー**:
  1. RestaurantApprovalRequest を Restaurant ドメインオブジェクトに変換
  2. RestaurantRepository で商品マスタ情報を取得
  3. RestaurantDomainService.validateOrder() を呼び出し
  4. OrderApprovalRepository で承認結果を保存
  5. RestaurantApprovalResponseMessagePublisher でイベントを発行

## 10. メッセージングの設計

### 10.1 Kafkaトピック
* **入力トピック**:
  * `restaurant-approval-request` - Order Serviceからの注文承認リクエスト

* **出力トピック**:
  * `restaurant-approval-response` - Order Serviceへの承認結果レスポンス

### 10.2 メッセージリスナー

#### RestaurantApprovalRequestKafkaListener
```java
@Slf4j
@Component
public class RestaurantApprovalRequestKafkaListener implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {
    
    private final RestaurantApprovalRequestMessageListener restaurantApprovalRequestMessageListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    
    @Override
    @KafkaListener(
        id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
        topics = "${restaurant-service.restaurant-approval-request-topic-name}"
    )
    public void receive(@Payload List<RestaurantApprovalRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant approval requests received with keys: {}, partitions: {} and offsets: {}",
                 messages.size(), keys.toString(), partitions.toString(), offsets.toString());
        
        messages.forEach(restaurantApprovalRequestAvroModel -> {
            try {
                log.info("Processing restaurant approval for order id: {}", 
                         restaurantApprovalRequestAvroModel.getOrderId());
                
                // Avroモデル → DTO 変換
                RestaurantApprovalRequest restaurantApprovalRequest = 
                    restaurantMessagingDataMapper.restaurantApprovalRequestAvroModelToRestaurantApproval(
                        restaurantApprovalRequestAvroModel);
                
                // ビジネスロジック層への委譲
                restaurantApprovalRequestMessageListener.approveOrder(restaurantApprovalRequest);
                
            } catch (DataAccessException e) {
                log.error("DataAccessException while processing restaurant approval for order id: {}", 
                          restaurantApprovalRequestAvroModel.getOrderId(), e);
                // Kafkaリトライメカニズムに任せる（例外を再スロー）
                throw new RuntimeException(e);
            }
        });
    }
}
```

* **配置**: `restaurant-messaging`モジュール
* **アノテーション**: `@Component`、`@KafkaListener`
* **役割**: **Kafka入力アダプター**
  - Kafkaトピックからメッセージを受信
  - Avroモデル（`RestaurantApprovalRequestAvroModel`）→ DTO（`RestaurantApprovalRequest`）への変換
  - ビジネスロジック層（`RestaurantApprovalRequestMessageListenerImpl`）への委譲
  - Kafka固有のエラーハンドリング（リトライ、デッドレターキュー）
  
* **依存関係**:
  - `RestaurantApprovalRequestMessageListener`: ポートインターフェース（application-serviceで定義）
  - `RestaurantMessagingDataMapper`: Avro ↔ DTO変換マッパー
  - `KafkaConsumer<T>`: 共通Kafkaコンシューマーインターフェース（kafka-consumerモジュール）

* **技術分離の境界**:
  - **このクラスより上層（application-service）**: Kafkaを知らない、Avroモデルを使わない
  - **このクラス**: Kafka固有の処理を集約（デシリアライズ、バッチ処理、オフセット管理）

* **設計意図**:
  - **アダプターパターンの実装**: Kafkaという具体的な技術をビジネスロジックから隠蔽
  - **単一責任の原則**: メッセージングプロトコルの変換のみを担当
  - **テスト戦略の分離**: 
    - このクラス: Kafka Testcontainersで結合テスト
    - `*MessageListenerImpl`: モックを使った単体テスト

### 10.3 メッセージパブリッシャー
```java
@Component
public class RestaurantApprovalResponseKafkaPublisher {
    public void publish(OrderApprovalEvent event) {
        // ドメインイベント → Avroモデルへの変換
        // Kafkaトピック "restaurant-approval-response" へ送信
    }
}
```

### 10.4 Avroスキーマ（restaurant-approval-response）
```json
{
  "type": "record",
  "name": "RestaurantApprovalResponseAvroModel",
  "fields": [
    {"name": "id", "type": "string"},
    {"name": "sagaId", "type": "string"},
    {"name": "restaurantId", "type": "string"},
    {"name": "orderId", "type": "string"},
    {"name": "createdAt", "type": "long"},
    {"name": "orderApprovalStatus", "type": "enum", "symbols": ["APPROVED", "REJECTED"]},
    {"name": "failureMessages", "type": {"type": "array", "items": "string"}}
  ]
}
```

## 11. データベース設計

### 11.1 スキーマ名
`restaurant`

### 11.2 テーブル設計

#### order_approval テーブル
```sql
CREATE TABLE restaurant.order_approval (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    order_id UUID NOT NULL UNIQUE,
    approval_status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurant.restaurants(id)
);

CREATE INDEX idx_order_approval_restaurant_id ON restaurant.order_approval(restaurant_id);
CREATE INDEX idx_order_approval_order_id ON restaurant.order_approval(order_id);
```

#### restaurants テーブル（マスタ）
```sql
CREATE TABLE restaurant.restaurants (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true
);
```

#### restaurant_products テーブル（マスタ）
```sql
CREATE TABLE restaurant.restaurant_products (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurant.restaurants(id),
    CONSTRAINT uk_restaurant_product UNIQUE (restaurant_id, product_id)
);

CREATE INDEX idx_restaurant_products_restaurant_id 
    ON restaurant.restaurant_products(restaurant_id);
```

### 11.3 マテリアライズドビュー（パフォーマンス最適化）
```sql
CREATE MATERIALIZED VIEW restaurant.order_restaurant_m_view AS
SELECT 
    r.id AS restaurant_id,
    r.name AS restaurant_name,
    r.active AS restaurant_active,
    p.product_id,
    p.name AS product_name,
    p.price AS product_price,
    p.available AS product_available
FROM restaurant.restaurants r
INNER JOIN restaurant.restaurant_products p ON r.id = p.restaurant_id;

CREATE UNIQUE INDEX idx_order_restaurant_m_view 
    ON restaurant.order_restaurant_m_view(restaurant_id, product_id);
```

* **用途**: 注文承認処理の高速化（商品マスタ結合の最適化）
* **更新**: バッチ処理または商品マスタ更新時にリフレッシュ

## 12. ビジネスルール

### 12.1 注文承認条件
以下のすべてを満たす場合に承認（APPROVED）:
1. 注文ステータスが PAID であること
2. 注文に含まれるすべての商品がレストランに存在すること
3. 各商品の商品名がレストランマスタと一致すること
4. 各商品の価格がレストランマスタと一致すること

### 12.2 注文拒否条件
以下のいずれかに該当する場合に拒否（REJECTED）:
1. 注文ステータスが PAID でない
2. 注文に含まれる商品がレストランに存在しない
3. 商品名がレストランマスタと一致しない
4. 商品価格がレストランマスタと一致しない

### 12.3 検証エラーメッセージ
* `"Payment is not completed for order: {orderId}"` - 支払い未完了
* `"Product with id: {productId} not found in restaurant: {restaurantId}"` - 商品不存在
* `"Product name does not match. Expected: {expected}, Found: {actual}"` - 商品名不一致
* `"Product price does not match. Expected: {expected}, Found: {actual}"` - 価格不一致

### 12.4 レストランのアクティブ状態
* レストランが `active = false` の場合、承認リクエストを処理しない
* RestaurantNotFoundExceptionをスローしてSagaにロールバックを通知

## 13. 統合フロー

### 13.1 正常フロー（承認）
```
1. Order Service: 注文の支払い完了（PAID）
   → Kafka: restaurant-approval-request

2. Restaurant Service: メッセージ受信
   → RestaurantApprovalRequestKafkaListener
   → RestaurantApprovalRequestMessageListener.approveOrder()

3. Restaurant Service: ビジネスロジック実行
   → RestaurantApprovalRequestHelper.persistOrderApproval()
   → RestaurantRepository で商品マスタ取得
   → Restaurant.validateOrder() で検証
   → 検証成功（failureMessages が空）

4. Restaurant Service: 承認処理
   → Restaurant.constructOrderApproval(APPROVED)
   → OrderApprovalRepository.save()
   → RestaurantDomainService.validateOrder()
   → OrderApprovedEvent 生成

5. Restaurant Service: イベント送信
   → RestaurantApprovalResponseMessagePublisher.publish()
   → Kafka: restaurant-approval-response

6. Order Service: 承認結果受信
   → 注文ステータスを APPROVED に更新
   → Sagaトランザクション完了
```

### 13.2 異常フロー（拒否）
```
1. Order Service: 注文の支払い完了（PAID）
   → Kafka: restaurant-approval-request

2. Restaurant Service: メッセージ受信
   → RestaurantApprovalRequestMessageListener.approveOrder()

3. Restaurant Service: ビジネスロジック実行
   → RestaurantRepository で商品マスタ取得
   → Restaurant.validateOrder() で検証
   → 検証失敗（failureMessages に追加）
     例: "Product price does not match. Expected: 50.00, Found: 60.00"

4. Restaurant Service: 拒否処理
   → Restaurant.constructOrderApproval(REJECTED)
   → OrderApprovalRepository.save()
   → RestaurantDomainService.validateOrder()
   → OrderRejectedEvent 生成

5. Restaurant Service: イベント送信
   → RestaurantApprovalResponseMessagePublisher.publish()
   → Kafka: restaurant-approval-response (失敗メッセージ含む)

6. Order Service: 拒否結果受信
   → 注文ステータスを CANCELLING に更新
   → Sagaトランザクション補償処理開始
   → Payment Service に返金リクエスト
```

### 13.3 例外フロー
```
1. レストランが見つからない場合:
   → RestaurantNotFoundException
   → Sagaに失敗通知（ロールバック）

2. データベース接続エラー:
   → Kafkaリトライメカニズム
   → 最大リトライ回数後、デッドレターキューに移動

3. Avroシリアライズ/デシリアライズエラー:
   → ログ記録
   → エラートピックに転送
```

## 14. Saga トランザクション連携

### 14.1 Sagaパターンの役割
Restaurant Serviceは、Order Serviceが管理するSagaオーケストレーションの参加者として動作します。

### 14.2 Saga ID
* **説明**: 一連のトランザクションを追跡するための一意な識別子
* **伝播**: Order Service → Restaurant Service → Order Service（同一sagaIdを使用）
* **用途**: ログ追跡、トランザクション状態管理、デバッグ

### 14.3 冪等性保証
* **問題**: Kafkaメッセージの重複配信により、同じ注文が複数回処理される可能性
* **解決策**:
  1. `order_id` に UNIQUE 制約
  2. `OrderApprovalRepository.save()` で `DataIntegrityViolationException` をキャッチ
  3. 既存レコードがあれば、既に処理済みとしてスキップ
  4. 冪等性を確保してイベントを再送信

### 14.4 補償トランザクション
Restaurant Serviceは補償トランザクションを実装しません。理由:
* 承認結果は履歴として保存されるが、実際の在庫引き当てや業務処理は行わない
* Order Serviceがロールバック時に自動的に注文をキャンセル
* 承認履歴は監査目的で保持

## 15. ドメインモデルの設計意図

### 15.1 Restaurant集約の責務
* **承認審査に特化**: レストラン自体のマスタ情報は持たず、審査対象注文のスナップショットに焦点
* **整合性チェック**: 商品構成・価格・支払い状態の検証をカプセル化
* **不変条件の保護**: OrderDetail（注文の一貫性を持つスナップショット）をfinalで保持

### 15.2 OrderApprovalの不変性
* 一度確定した承認結果は変更されない
* 履歴として保存され、監査やトレーサビリティを担保

### 15.3 ドメインサービスの役割
* エンティティ単独では表現できない、複数エンティティにまたがるビジネスロジック
* イベント生成とパブリッシャーの関連付け
* Springフレームワークから独立した純粋なドメインロジック

## 16. 非機能要件

### 16.1 パフォーマンス
* 承認処理: 500ms以内
* データベースクエリ: マテリアライズドビューによる最適化
* Kafka メッセージ処理: 非同期処理で高スループット

### 16.2 可用性
* サービス稼働率: 99.9%
* Kafkaメッセージ処理の冪等性保証
* データベース接続プールによる効率化

### 16.3 スケーラビリティ
* 複数インスタンスでの水平スケーリング対応
* Kafka Consumer Groupによる並列処理
* ステートレスな設計（承認処理はトランザクション単位）

### 16.4 セキュリティ
* マイクロサービス間通信: Kafka ACL（Access Control List）
* データベースアクセス: 最小権限の原則
* 機密情報のログ出力禁止

## 17. 今後の拡張ポイント

### 17.1 在庫管理機能
* 商品の在庫数を管理
* 注文承認時に在庫引き当て
* 在庫不足時の自動拒否

### 17.2 営業時間チェック
* レストランの営業時間をマスタ管理
* 営業時間外の注文は自動拒否

### 17.3 配達可能エリアチェック
* レストランごとの配達可能エリアを定義
* 配達先住所が範囲外の場合は自動拒否

### 17.4 動的価格設定
* 時間帯やキャンペーンに応じた動的価格
* 注文時点の価格スナップショットで検証

### 17.5 承認ワークフロー
* 自動承認と手動承認の選択機能
* レストラン担当者による承認UI
* 承認タイムアウト処理

### 17.6 レポーティング
* 承認/拒否率のダッシュボード
* 拒否理由の分析レポート
* レストランパフォーマンス指標

## 18. テストデータ

### 18.1 レストランID
* Restaurant 1: `d215b5f8-0249-4dc5-89a3-51fd148cfb45`
* Restaurant 2: `d215b5f8-0249-4dc5-89a3-51fd148cfb46`

### 18.2 商品マスタ
#### Restaurant 1
* Product 1: `d215b5f8-0249-4dc5-89a3-51fd148cfb48`, "Product-1", 50.00円, available=true
* Product 2: `d215b5f8-0249-4dc5-89a3-51fd148cfb49`, "Product-2", 100.00円, available=true

#### Restaurant 2
* Product 3: `d215b5f8-0249-4dc5-89a3-51fd148cfb50`, "Product-3", 75.00円, available=true
* Product 4: `d215b5f8-0249-4dc5-89a3-51fd148cfb51`, "Product-4", 150.00円, available=false

### 18.3 テストシナリオ用データ
* **承認ケース**: Restaurant 1, Product 1 (50.00円) x2 + Product 2 (100.00円) x1 = 200.00円
* **拒否ケース（価格不一致）**: Restaurant 1, Product 1を60.00円で注文
* **拒否ケース（商品不存在）**: Restaurant 1に存在しない商品IDで注文
* **拒否ケース（商品利用不可）**: Restaurant 2, Product 4（available=false）で注文

## 19. ログ設計

### 19.1 ログレベル
* DEBUG: ドメインロジック詳細、メッセージ受信/送信
* INFO: 承認/拒否結果、トランザクション開始/完了
* WARN: 検証失敗、リトライ
* ERROR: 例外発生、データベースエラー

### 19.2 ログ出力例
```
INFO  - Validating order: {orderId} for restaurant: {restaurantId}
DEBUG - Found restaurant information with products: {productCount}
WARN  - Order validation failed: {failureMessages}
INFO  - Order approved for restaurant: {restaurantId}, order: {orderId}
INFO  - Order rejected for restaurant: {restaurantId}, order: {orderId}, reasons: {failureMessages}
ERROR - Restaurant not found: {restaurantId}
```

### 19.3 ログフィールド
* sagaId: Sagaトランザクション追跡
* orderId: 注文追跡
* restaurantId: レストラン追跡
* createdAt: イベント発生時刻
* failureMessages: エラー詳細

## 20. 依存関係管理

### 20.1 restaurant-domain-core
* **依存可能**: common-domain のみ
* **依存禁止**: Spring Framework、Jakarta Persistence等のフレームワーク

### 20.2 restaurant-application-service
* **依存可能**: 
  - restaurant-domain-core
  - common-domain
  - spring-boot-starter-validation
  - spring-tx
* **依存禁止**: データベース、Kafka等のインフラ依存

### 20.3 restaurant-dataaccess
* **依存可能**:
  - restaurant-application-service
  - spring-boot-starter-data-jpa
  - postgresql
* **責務**: ポートの実装（アダプター）

### 20.4 restaurant-messaging
* **依存可能**:
  - restaurant-application-service
  - kafka-producer
  - kafka-consumer
  - kafka-model
* **責務**: Kafkaメッセージング実装

### 20.5 restaurant-container
* **依存可能**: すべてのrestaurant-serviceモジュール
* **責務**: Spring Boot設定、Bean登録、アプリケーション起動

## 21. 参考資料

### 21.1 関連設計書
* `_Design/payment-service_design.md` - Payment Serviceの設計（Sagaパターン参考）
* `_Design/delivery-service_design.md` - Delivery Serviceの設計（モジュール構成参考）
* `_Design/CodingRule.md` - コーディング規約

### 21.2 UML図
* `_Design/uml/OrderDomainClass.pu` - 注文ドメインクラス図
* `_Design/uml/PublisherSequence.pu` - メッセージパブリッシャーシーケンス図
* `_Design/uml/ListenerSequence.pu` - メッセージリスナーシーケンス図
