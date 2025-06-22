
# payment-serivice の設計

Udemy-44.Domain core module:Aggregate root, entity, value object より  

## moduleの構成
以下の module を maven module として作成する。  
* payment-service
  * payment-container
  * payment-dataaccess
  * payment-domain
    * payment-domain-core
    * payment-application-service
  * payment-messaging

## packageの構成
### order-domain-core module
* com.food.ordering.system.order.service.domain
  * entity
  * valueObject
  * event
  * exception

## entity の設計
### Payment エンティティ
* フィールド
  * PaymentId paymentId  # AggregateRoot, BaseEntityから継承
  * OrderId orderId  # 支払い対象のOrderを示す
  * CustomerId customerId  # 注文者のCustomerを示す。但し、OrderのCustomerIdと冗長。同一になるはず。
  * Money price  # 支払い金額
  * PaymentStatus paymentStatus
  * ZonedDateTime createdAt
* メソッド
  * initializePayment()
  * validatePayment()
* インスタンス生成のパターン
  * Builderパターンを適用
 
### CreditEntry エンティティ
* フィールド
  * CustomerId
  * TotalCreditAmount
* メソッド
  * addCreditAmount()
  * substractCreditAmount()
* インスタンス生成のパターン
  * Builderパターンを適用

### 

## valueObject の設計
### PaymentId
* common-domain モジュールの BaseId を継承
* 型は、UUID
### CreditEntryId
* common-domain モジュールの BaseId を継承
* 型は、UUID
### CreditHisotryId
* common-domain モジュールの BaseId を継承
* 型は、UUID