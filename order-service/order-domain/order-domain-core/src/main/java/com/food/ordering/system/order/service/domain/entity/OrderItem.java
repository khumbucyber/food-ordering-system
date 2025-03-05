package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.OrderId;
import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderItem extends BaseEntity<OrderItemId> {
    private OrderId orderId;
    private final Product product;
    private final int quantity;
    private final Money price;
    private final Money subTotal;

    // Orderエンティティのみからアクセスするので、パッケージプライベートにする(Udemy-15 2:45)
    void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
        this.orderId = orderId;
        super.setId(orderItemId);
    }

    public boolean isPriceValid() {
        log.info("orderItemのprice:{}, productのprice:{}", price.getAmount(), product.getPrice());
        return price.isGreaterThanZero() &&
//                price.equals(product.getPrice()) &&
                price.multiply(quantity).equals(subTotal);
    }

    // privateなコンストラクタ
    private OrderItem(Builder builder) {
        super.setId(builder.orderItemId);
        this.product = builder.product;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.subTotal = builder.subTotal;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    public Product getProduct() {
        return product;
    }
    public int getQuantity() {
        return quantity;
    }
    public Money getPrice() {
        return price;
    }
    public Money getSubTotal() {
        return subTotal;
    }

    // builderオブジェクトを生成するためのメソッド
    // なくても new OrderItem.Builder() でデフォルトコンストラクタを呼びオブジェクト生成はできる
    public static Builder builder() {
        return new Builder();
    }

    // Builderクラス
    // privateなOrderItemコンストラクタに渡すbuilderオブジェクトを構築するためのクラス
    public static class Builder {
        private OrderItemId orderItemId;
        private Product product;
        private int quantity;
        private Money price;
        private Money subTotal;
    
        public Builder setOrderItemId(OrderItemId orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }
        public Builder setProduct(Product product) {
            this.product = product;
            return this;
        }
        public Builder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
        public Builder setPrice(Money price) {
            this.price = price;
            return this;
        }
        public Builder setSubTotal(Money subTotal) {
            this.subTotal = subTotal;
            return this;
        }
        // OrderItemクラスのprivateなコンストラクタを呼んで
        // OrderItemオブジェクトを生成するメソッド
        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}
