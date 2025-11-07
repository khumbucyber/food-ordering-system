package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

/*
 * OrderApproval Entityクラス
 * レストランの注文承認を表すエンティティ
 * フィールド:
 * - restaurantId: レストランの識別子
 * - orderId: 注文の識別子
 * - approvalStatus: 注文承認のステータス
 * 使われ方:
 * - 注文がレストランによって承認または拒否されたことを表すために使用される
 *  - 注文承認の状態を管理し、関連するビジネスロジックを実装するために使用される
 */
public class OrderApproval extends BaseEntity<OrderApprovalId> {

    private final RestaurantId restaurantId;
    private final OrderId orderId;
    private final OrderApprovalStatus approvalStatus;

    private OrderApproval(Builder builder) {
        super.setId(builder.orderApprovalId);
        restaurantId = builder.restaurantId;
        orderId = builder.orderId;
        approvalStatus = builder.approvalStatus;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public OrderApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderApprovalId orderApprovalId;
        private RestaurantId restaurantId;
        private OrderId orderId;
        private OrderApprovalStatus approvalStatus;

        public Builder orderApprovalId(OrderApprovalId orderApprovalId) {
            this.orderApprovalId = orderApprovalId;
            return this;
        }

        public Builder restaurantId(RestaurantId restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }

        public Builder orderId(OrderId orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder approvalStatus(OrderApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public OrderApproval build() {
            return new OrderApproval(this);
        }
    }
}
