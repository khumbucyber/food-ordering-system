# delivery-service の設計

## 1. 概要

### 1.1 デリバリードメインの役割
デリバリーサービスは、レストランで承認された注文の配達業務を管理するマイクロサービスです。
配達員の割り当て、配達ステータスの管理、配達完了/失敗の処理を担当します。

### 1.2 ビジネスフロー
1. Restaurant Service から注文承認完了イベントを受信
2. 利用可能な配達員を検索し、配達を割り当て
3. 配達員が配達員用モバイルアプリから商品ピックアップを報告
4. 配達員が配達先に向かう
5. 配達員がモバイルアプリから配達完了または配達失敗を報告
6. Order Service に配達結果を通知

## 2. モジュールの構成

```
delivery-service
├── delivery-container          # Spring Boot アプリケーション、設定
├── delivery-dataaccess         # JPA エンティティ、リポジトリ実装
├── delivery-domain
│   ├── delivery-domain-core           # ドメインエンティティ、値オブジェクト、ドメインサービス
│   └── delivery-application-service   # アプリケーションサービス、ポート、DTO
└── delivery-messaging          # Kafka メッセージング
```

## 3. パッケージの構成

### 3.1 delivery-domain-core module
```
com.food.ordering.system.delivery.service.domain
├── entity/          # エンティティ
├── valueobject/     # 値オブジェクト
├── event/           # ドメインイベント
└── exception/       # ドメイン例外
```

### 3.2 delivery-application-service module
```
com.food.ordering.system.delivery.service.domain
├── dto/             # データ転送オブジェクト
├── ports/
│   ├── input/       # ユースケース（インターフェース）
│   └── output/      # リポジトリ、メッセージパブリッシャー（インターフェース）
└── mapper/          # DTO ↔ Entity マッパー
```

## 4. エンティティの設計

### 4.1 Delivery エンティティ（集約ルート）
* **継承**: `AggregateRoot<DeliveryId>`
* **フィールド**:
  * `DeliveryId id` - 配達ID（AggregateRootから継承）
  * `OrderId orderId` - 対象の注文ID
  * `RestaurantId restaurantId` - レストランID（ピックアップ場所）
  * `DeliveryDriverId deliveryDriverId` - 割り当てられた配達員ID
  * `DeliveryAddress pickupAddress` - ピックアップ先住所
  * `DeliveryAddress deliveryAddress` - 配達先住所
  * `DeliveryStatus deliveryStatus` - 配達ステータス
  * `ZonedDateTime requestedAt` - 配達リクエスト時刻
  * `ZonedDateTime assignedAt` - 配達員割り当て時刻
  * `ZonedDateTime pickedUpAt` - ピックアップ時刻
  * `ZonedDateTime deliveredAt` - 配達完了時刻
  * `String failureMessage` - 配達失敗時のメッセージ
  * `List<String> failureMessages` - 検証エラーメッセージのリスト

* **メソッド**:
  * `void initializeDelivery()` - 配達の初期化（ステータス: PENDING）
  * `void assignDriver(DeliveryDriverId driverId, ZonedDateTime assignedAt)` - 配達員の割り当て
  * `void pickUp(ZonedDateTime pickedUpAt)` - ピックアップ処理
  * `void complete(ZonedDateTime deliveredAt)` - 配達完了
  * `void cancel(String reason)` - 配達キャンセル
  * `void validateDelivery()` - 配達の検証

* **インスタンス生成**: Builderパターン

### 4.2 DeliveryDriver エンティティ
* **継承**: `BaseEntity<DeliveryDriverId>`
* **フィールド**:
  * `DeliveryDriverId id` - 配達員ID
  * `String name` - 配達員名
  * `String phoneNumber` - 電話番号
  * `DriverStatus status` - 配達員ステータス（AVAILABLE, BUSY, OFFLINE）
  * `int activeDeliveryCount` - 現在の配達件数

* **メソッド**:
  * `boolean isAvailable()` - 割り当て可能かチェック
  * `void assignDelivery()` - 配達割り当て（ステータス: BUSY、activeDeliveryCount++）
  * `void completeDelivery()` - 配達完了処理（activeDeliveryCount--、count==0ならAVAILABLE）
  * `void validateDriver()` - 配達員の検証

* **インスタンス生成**: Builderパターン

## 5. 値オブジェクトの設計

### 5.1 DeliveryId
* `BaseId<UUID>` を継承
* 型: `UUID`

### 5.2 DeliveryDriverId
* `BaseId<UUID>` を継承
* 型: `UUID`

### 5.3 DeliveryStatus（列挙型）
```java
public enum DeliveryStatus {
    PENDING,        // 配達員割り当て待ち
    ASSIGNED,       // 配達員割り当て済み
    PICKED_UP,      // ピックアップ完了
    IN_TRANSIT,     // 配達中
    DELIVERED,      // 配達完了
    CANCELLED       // キャンセル
}
```

### 5.4 DriverStatus（列挙型）
```java
public enum DriverStatus {
    AVAILABLE,      // 配達可能
    BUSY,           // 配達中
    OFFLINE         // オフライン
}
```

### 5.5 DeliveryAddress
* **フィールド**:
  * `UUID id` - アドレスID
  * `String street` - 番地
  * `String postalCode` - 郵便番号
  * `String city` - 市区町村

* **イミュータブル**: すべてのフィールドが `final`

## 6. ドメインイベントの設計

### 6.1 DeliveryRequestedEvent
* レストラン承認完了後に発行
* ペイロード: `orderId`, `restaurantId`, `deliveryAddress`

### 6.2 DeliveryAssignedEvent
* 配達員割り当て時に発行
* ペイロード: `deliveryId`, `orderId`, `deliveryDriverId`, `assignedAt`

### 6.3 DeliveryPickedUpEvent
* ピックアップ完了時に発行
* ペイロード: `deliveryId`, `orderId`, `pickedUpAt`

### 6.4 DeliveryCompletedEvent
* 配達完了時に発行
* ペイロード: `deliveryId`, `orderId`, `deliveredAt`

### 6.5 DeliveryCancelledEvent
* 配達キャンセル時に発行
* ペイロード: `deliveryId`, `orderId`, `reason`, `cancelledAt`

すべてのイベントは `DomainEvent<T>` を実装し、イミュータブルです。

## 7. ドメインサービスの設計

### 7.1 DeliveryDomainService（インターフェース）
```java
public interface DeliveryDomainService {
    DeliveryAssignedEvent assignDelivery(Delivery delivery, List<DeliveryDriver> availableDrivers);
    DeliveryPickedUpEvent pickUpDelivery(Delivery delivery);
    DeliveryCompletedEvent completeDelivery(Delivery delivery);
    DeliveryCancelledEvent cancelDelivery(Delivery delivery, String reason);
}
```

### 7.2 DeliveryDomainServiceImpl（実装）
* **ビジネスロジック**:
  * 配達員の割り当てアルゴリズム（現在の配達件数が最も少ない配達員を選択）
  * 配達ステータスの遷移検証
  * 配達時間の妥当性チェック

## 8. ドメイン例外の設計

### 8.1 DeliveryDomainException
* 基底となるドメイン例外

### 8.2 DeliveryNotFoundException
* 配達が見つからない場合

### 8.3 NoAvailableDriverException
* 利用可能な配達員がいない場合

### 8.4 InvalidDeliveryStatusException
* 無効なステータス遷移

## 9. アプリケーションサービスの設計

### 9.1 ポート（入力）
```java
public interface DeliveryRequestHandler {
    DeliveryResponse requestDelivery(DeliveryRequest request);
}

public interface DeliveryCommandHandler {
    // 配達員用モバイルアプリから呼び出されるAPI
    DeliveryResponse pickUpDelivery(UUID deliveryId);
    DeliveryResponse completeDelivery(UUID deliveryId);
    DeliveryResponse cancelDelivery(UUID deliveryId, String reason);
}

public interface DeliveryTrackingHandler {
    DeliveryTrackingResponse trackDelivery(UUID orderId);
}
```

### 9.2 ポート（出力）
```java
public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(DeliveryId deliveryId);
    Optional<Delivery> findByOrderId(OrderId orderId);
}

public interface DeliveryDriverRepository {
    List<DeliveryDriver> findAvailableDrivers();
    Optional<DeliveryDriver> findById(DeliveryDriverId driverId);
    DeliveryDriver save(DeliveryDriver driver);
}

public interface DeliveryResponseMessagePublisher {
    void publish(DeliveryCompletedEvent event);
    void publish(DeliveryCancelledEvent event);
}
```

### 9.3 DTO設計
```java
public class DeliveryRequest {
    private UUID orderId;
    private UUID restaurantId;
    private AddressDto pickupAddress;
    private AddressDto deliveryAddress;
    private ZonedDateTime requestedAt;
}

public class DeliveryResponse {
    private UUID deliveryId;
    private UUID orderId;
    private DeliveryStatus status;
    private String message;
}

public class DeliveryTrackingResponse {
    private UUID deliveryId;
    private UUID orderId;
    private DeliveryStatus status;
    private String driverName;
    private String driverPhone;
    private ZonedDateTime assignedAt;
    private ZonedDateTime pickedUpAt;
    private ZonedDateTime estimatedDeliveryTime;
}
```

## 10. メッセージングの設計

### 10.1 Kafkaトピック
* **入力トピック**:
  * `restaurant-approval-response` - レストラン承認結果を受信

* **出力トピック**:
  * `delivery-response` - 配達結果を送信

### 10.2 メッセージリスナー
```java
@Component
public class RestaurantApprovalResponseMessageListener {
    // restaurant-approval-responseトピックから受信
    // 承認された注文の配達リクエストを作成
}
```

### 10.3 メッセージパブリッシャー
```java
@Component
public class DeliveryResponseKafkaPublisher {
    // delivery-responseトピックに配達結果を送信
}
```

## 11. データベース設計

### 11.1 スキーマ名
`delivery`

### 11.2 テーブル設計

#### deliveries テーブル
```sql
CREATE TABLE delivery.deliveries (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    restaurant_id UUID NOT NULL,
    driver_id UUID,
    pickup_address_id UUID NOT NULL,
    delivery_address_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    failure_message TEXT,
    CONSTRAINT fk_driver FOREIGN KEY (driver_id) REFERENCES delivery.drivers(id),
    CONSTRAINT fk_pickup_address FOREIGN KEY (pickup_address_id) REFERENCES delivery.addresses(id),
    CONSTRAINT fk_delivery_address FOREIGN KEY (delivery_address_id) REFERENCES delivery.addresses(id)
);

CREATE INDEX idx_deliveries_order_id ON delivery.deliveries(order_id);
CREATE INDEX idx_deliveries_status ON delivery.deliveries(status);
CREATE INDEX idx_deliveries_driver_id ON delivery.deliveries(driver_id);
```

#### drivers テーブル
```sql
CREATE TABLE delivery.drivers (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    active_delivery_count INT DEFAULT 0,
    CONSTRAINT chk_active_delivery_count CHECK (active_delivery_count >= 0)
);

CREATE INDEX idx_drivers_status ON delivery.drivers(status);
```

#### addresses テーブル
```sql
CREATE TABLE delivery.addresses (
    id UUID PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL
);
```

## 12. ビジネスルール

### 12.1 配達員割り当てロジック
1. ステータスが `AVAILABLE` の配達員を取得
2. 現在の配達件数（activeDeliveryCount）が最も少ない配達員を選択
3. 配達員のステータスを `BUSY` に更新、activeDeliveryCount をインクリメント
4. 配達のステータスを `ASSIGNED` に更新

### 12.2 ステータス遷移ルール
```
PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
            ↓           ↓           ↓
         CANCELLED   CANCELLED   CANCELLED
```

### 12.3 配達キャンセル条件
* レストランが注文をキャンセルした場合
* 顧客が配達前にキャンセルした場合
* 配達員が対応不可になった場合
* 配達先に到達できない場合

### 12.4 配達時間の制約
* ピックアップ時刻 > 割り当て時刻
* 配達完了時刻 > ピックアップ時刻
* 配達完了は、ピックアップから2時間以内を推奨

## 13. 統合フロー

### 13.1 正常フロー
```
1. Restaurant Service: 注文承認完了
   → Kafka: restaurant-approval-response

2. Delivery Service: メッセージ受信
   → 配達リクエスト作成（PENDING）
   → 配達員を検索・割り当て（ASSIGNED）
   → DeliveryAssignedEvent 発行

3. 配達員: 配達員用モバイルアプリでレストランに到着、ピックアップ完了
   → モバイルアプリ: POST /api/v1/deliveries/{deliveryId}/pickup
   → Delivery Service: pickUp処理実行（PICKED_UP）
   → DeliveryPickedUpEvent 発行

4. 配達員: 配達先に向かう（IN_TRANSIT）

5. 配達員: 配達先に到着、配達完了
   → モバイルアプリ: POST /api/v1/deliveries/{deliveryId}/complete
   → Delivery Service: complete処理実行（DELIVERED）
   → DeliveryCompletedEvent 発行
   → Kafka: delivery-response

6. Order Service: 配達完了受信
   → 注文ステータスを DELIVERED に更新
```

### 13.2 異常フロー
```
1. 配達員が見つからない場合:
   → NoAvailableDriverException
   → 配達ステータスはPENDINGのまま
   → 定期的にリトライ（バックグラウンドジョブ）

2. 配達員がキャンセルした場合:
   → モバイルアプリ: POST /api/v1/deliveries/{deliveryId}/cancel
   → 配達を CANCELLED に更新
   → 別の配達リクエストとして再作成（オプション）

3. 配達失敗の場合:
   → DeliveryCancelledEvent 発行
   → Kafka: delivery-response（失敗）
   → Order Service: 注文ステータスを DELIVERY_FAILED に更新
```

## 14. API設計（REST）

### 14.1 配達トラッキング（顧客用）
```
GET /api/v1/deliveries/track/{orderId}
Response: DeliveryTrackingResponse
```

### 14.2 ピックアップ完了（配達員用モバイルアプリ）
```
POST /api/v1/deliveries/{deliveryId}/pickup
Response: DeliveryResponse
```

### 14.3 配達完了（配達員用モバイルアプリ）
```
POST /api/v1/deliveries/{deliveryId}/complete
Response: DeliveryResponse
```

### 14.4 配達キャンセル（配達員用モバイルアプリ）
```
POST /api/v1/deliveries/{deliveryId}/cancel
Request: { reason }
Response: DeliveryResponse
```

### 14.5 配達員の配達一覧取得（配達員用モバイルアプリ）
```
GET /api/v1/deliveries/driver/{driverId}
Response: List<DeliveryResponse>
```

## 15. 配達員用モバイルアプリとの連携

### 15.1 モバイルアプリの役割
* 配達員の認証・認可
* 割り当てられた配達の一覧表示
* ピックアップ先・配達先の住所表示
* ピックアップ完了ボタン → pickup API呼び出し
* 配達完了ボタン → complete API呼び出し
* キャンセルボタン → cancel API呼び出し
* 顧客への電話連絡（電話番号の表示）

### 15.2 API認証
* JWT（JSON Web Token）を使用
* 配達員ごとに一意のトークンを発行
* APIリクエストのAuthorizationヘッダーにトークンを含める

## 16. 非機能要件

### 16.1 パフォーマンス
* 配達員割り当て: 3秒以内
* トラッキング情報取得: 1秒以内
* モバイルアプリからのAPI呼び出し: 2秒以内

### 16.2 可用性
* サービス稼働率: 99.9%
* Kafkaメッセージ処理の冪等性保証

### 16.3 スケーラビリティ
* 複数インスタンスでの水平スケーリング対応
* Kafka Consumer Groupによる負荷分散

## 17. 今後の拡張ポイント

* 配達員評価システム
* 配達時間予測機能
* 複数注文の同時配達（バッチ配達）
* 配達料金計算機能
* プッシュ通知機能（配達員・顧客への通知）
* リアルタイム位置追跡（将来的にGPS連携）
