# Restaurant Service 実装タスクリスト

## 進捗状況
- 完了: 5/17
- 進行中: 0/17
- 未着手: 12/17

---

## ✅ 完了したタスク

### 1. restaurant-serviceのモジュール構造作成
**ステータス**: ✅ 完了

**内容**:
- restaurant-service配下に以下のモジュールディレクトリを作成:
  - restaurant-domain/restaurant-domain-core
  - restaurant-domain/restaurant-application-service
  - restaurant-dataaccess
  - restaurant-messaging
  - restaurant-container

---

### 2. ベースpom.xml作成
**ステータス**: ✅ 完了

**内容**:
- restaurant-service/pom.xmlを作成
- モジュール定義とプロパティを設定
- order-service/payment-serviceと同様の構造

---

### 3. restaurant-domain-core: pom.xml作成
**ステータス**: ✅ 完了

**内容**:
- common-domain依存関係を含むpom.xmlを作成
- restaurant-domain/pom.xmlも作成（親POM）

---

### 4. restaurant-domain-core: ドメインモデル実装
**ステータス**: ✅ 完了

**作成したファイル**:
- エンティティ:
  - `Restaurant.java` - レストラン集約ルート
  - `Product.java` - 商品エンティティ
  - `OrderDetail.java` - 注文詳細エンティティ
  - `OrderApproval.java` - 注文承認エンティティ
- 値オブジェクト:
  - `OrderApprovalId.java`
- イベント:
  - `OrderApprovalEvent.java` (抽象クラス)
  - `OrderApprovedEvent.java` (承認イベント)
  - `OrderRejectedEvent.java` (拒否イベント)

---

### 5. restaurant-domain-core: ドメインサービス・例外実装
**ステータス**: ✅ 完了

**作成したファイル**:
- ドメインサービス:
  - `RestaurantDomainService.java` (インターフェース)
  - `RestaurantDomainServiceImpl.java` (実装 - 注文検証ロジック)
- 例外クラス:
  - `RestaurantDomainException.java`
  - `RestaurantNotFoundException.java`

---

## 🔲 未着手のタスク

### 6. restaurant-application-service: pom.xml作成
**ステータス**: ⬜ 未着手

**内容**:
- domain-core, common-domain, spring-boot-starter-validation, spring-tx依存関係を含むpom.xmlを作成

---

### 7. restaurant-application-service: ポート・DTO実装
**ステータス**: ⬜ 未着手

**内容**:
- 入力ポート: `RestaurantApprovalRequestMessageListener`
- 出力ポート: 
  - `RestaurantApprovalResponseMessagePublisher`
  - `RestaurantRepository`
- DTOクラス:
  - `RestaurantApprovalRequest`
  - `RestaurantApprovalResponse`等

---

### 8. restaurant-application-service: サービス・ヘルパー・マッパー実装
**ステータス**: ⬜ 未着手

**内容**:
- アプリケーションサービス: `RestaurantApprovalRequestMessageListenerImpl`
- ヘルパー: `RestaurantApprovalRequestHelper`
- マッパー: ドメインエンティティ⇔DTO変換

---

### 9. restaurant-dataaccess: pom.xml作成
**ステータス**: ⬜ 未着手

**内容**:
- application-service, spring-boot-starter-data-jpa, postgresql依存関係を含むpom.xmlを作成

---

### 10. restaurant-dataaccess: エンティティ・リポジトリ・アダプター実装
**ステータス**: ⬜ 未着手

**内容**:
- JPAエンティティ:
  - `RestaurantEntity`
  - `ProductEntity`
  - `OrderApprovalEntity`等
- JPAリポジトリ:
  - `RestaurantJpaRepository`等
- アダプター:
  - `RestaurantRepositoryImpl`
- マッパー: ドメイン⇔JPA変換

---

### 11. restaurant-messaging: pom.xml作成
**ステータス**: ⬜ 未着手

**内容**:
- application-service, kafka-producer, kafka-consumer, kafka-model依存関係を含むpom.xmlを作成

---

### 12. restaurant-messaging: リスナー・パブリッシャー・マッパー実装
**ステータス**: ⬜ 未着手

**内容**:
- Kafkaリスナー:
  - `RestaurantApprovalRequestKafkaListener` (restaurant-approval-request受信)
- パブリッシャー:
  - `RestaurantApprovalResponseKafkaPublisher` (restaurant-approval-response送信)
- マッパー: Avro⇔DTO変換
- 設定クラス

---

### 13. restaurant-container: pom.xml作成
**ステータス**: ⬜ 未着手

**内容**:
- domain-core, application-service, dataaccess, messaging, spring-boot-starter依存関係
- spring-boot-maven-plugin設定を含むpom.xmlを作成

---

### 14. restaurant-container: Javaクラス作成
**ステータス**: ⬜ 未着手

**内容**:
- `RestaurantServiceApplication.java`
  - @SpringBootApplication
  - @EnableJpaRepositories
  - @EntityScan
- `BeanConfiguration.java`
  - RestaurantDomainServiceのBean登録

---

### 15. restaurant-container: application.yml作成
**ステータス**: ⬜ 未着手

**内容**:
- サーバーポート: 8183
- DB接続: restaurantスキーマ
- Kafkaトピック設定

---

### 16. restaurant-container: init-schema.sql作成
**ステータス**: ⬜ 未着手

**内容**:
- restaurantスキーマ
- テーブル:
  - restaurants
  - products
  - order_approval
- approval_status ENUM定義

---

### 17. restaurant-container: init-data.sql作成
**ステータス**: ⬜ 未着手

**内容**:
- テスト用レストランと商品データ2セット
- order-serviceのリクエストと整合性を保つ

---

## 📝 メモ

- Payment Serviceのパターンを参考に実装
- Order Serviceとの連携を考慮したデータ設計
- Kafkaトピック:
  - 受信: restaurant-approval-request
  - 送信: restaurant-approval-response
