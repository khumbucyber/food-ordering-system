package com.food.ordering.system.restaurant.service.domain.entity;

import java.util.List;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

/**
 * 注文承認に参加するパートナーレストランを表す集約ルート。
 * <p>
 * 承認ワークフローを集約内で完結させるため、審査対象となる OrderDetail を保持し、
 * レストラン視点で注文内容（商品構成・価格・支払い状態）を検証して不整合を防ぐ。
 * この集約はレストラン自体のマスタ情報を保持せず、審査対象となる注文スナップショットを取り込んで
 * レストラン視点で与信・整合性チェックを行うためのステートに特化している。
 */
public class Restaurant extends AggregateRoot<RestaurantId> {

    private OrderApproval orderApproval;
    private boolean active;
    private final OrderDetail orderDetail;

    private Restaurant(Builder builder) {
        super.setId(builder.restaurantId);
        orderApproval = builder.orderApproval;
        active = builder.active;
        orderDetail = builder.orderDetail;
    }

    public void validateOrder(List<String> failureMessages) {
        if (orderDetail.getOrderStatus() != OrderStatus.PAID) {
            failureMessages.add("Payment is not completed for order: " + orderDetail.getId().getValue());
        }
        orderDetail.getProducts().forEach(product -> {
            Product restaurantProduct = orderDetail.getProducts().stream()
                    .filter(p -> p.getId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);
            if (restaurantProduct == null) {
                failureMessages.add("Product with id: " + product.getId().getValue() + 
                        " not found in restaurant: " + getId().getValue());
            } else if (!product.getName().equals(restaurantProduct.getName())) {
                failureMessages.add("Product name does not match. Expected: " + 
                        restaurantProduct.getName() + ", Found: " + product.getName());
            } else if (!product.getPrice().equals(restaurantProduct.getPrice())) {
                failureMessages.add("Product price does not match. Expected: " + 
                        restaurantProduct.getPrice() + ", Found: " + product.getPrice());
            }
        });
    }

    public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
        this.orderApproval = OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(orderDetail.getId().getValue()))
                .restaurantId(getId())
                .orderId(orderDetail.getId())
                .approvalStatus(orderApprovalStatus)
                .build();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public boolean isActive() {
        return active;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RestaurantId restaurantId;
        private OrderApproval orderApproval;
        private boolean active;
        private OrderDetail orderDetail;

        public Builder restaurantId(RestaurantId restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }

        public Builder orderApproval(OrderApproval orderApproval) {
            this.orderApproval = orderApproval;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder orderDetail(OrderDetail orderDetail) {
            this.orderDetail = orderDetail;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
