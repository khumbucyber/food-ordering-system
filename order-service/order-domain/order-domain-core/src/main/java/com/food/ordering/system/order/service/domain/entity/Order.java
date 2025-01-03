package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.OrderId;
import com.food.ordering.system.domain.ValueObject.RestaurantId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.OrderStatus;
import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {
    private final CustomerId customerId;
    private final RestaurantId restaurantId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;

    public void initializeOrder() {
        setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        initializeOrderItem();
    }

    public void validateOrder() {
        validateInitialOrder();
        validateTotalPrice();
        validateItemsPrice();

    }

    public void pay() {
        if (orderStatus != OrderStatus.PENDING) {
            throw new OrderDomainException("Order is not in correct state for pay operation");
        }
        orderStatus = OrderStatus.PAID;
    }

    public void approve() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for approve operation");
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel() {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for initCancel operation");
        }
        orderStatus = OrderStatus.CANCELLED;
    }

    private void initializeOrderItem() {
        long itemId = 1;
        for (OrderItem orderItem: items) {
            orderItem.initializeOrderItem(super.getId(), new OrderItemId(itemId++));
        }
    }

    private void validateInitialOrder() {
        if (orderStatus != null || super.getId() != null) {
            throw new OrderDomainException("Order is not in correct state for initialization!");
        }
    }

    private void validateTotalPrice() {
        if (price == null || !price.isGreaterThanZero()) {
            throw new OrderDomainException("Total price must be greater than zero!");
        }
    }

    private void validateItemsPrice() {
        Money orderItemsTotal = items.stream().map(orderItem -> {
            validateItemPrice(orderItem);
            return orderItem.getSubTotal();
        }).reduce(Money.ZERO, Money::add);

        if (price.equals(orderItemsTotal)) {
            throw new OrderDomainException("Total Price: " + price.getAmount()
                + " is not equal to Order items total: " + orderItemsTotal.getAmount() + " !");
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        this.customerId = builder.customerId;
        this.restaurantId = builder.restaurantId;
        this.deliveryAddress = builder.deliveryAddress;
        this.price = builder.price;
        this.items = builder.items;
        this.trackingId = builder.trackingId;
        this.orderStatus = builder.orderStatus;
        this.failureMessages = builder.failureMessages;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
    public RestaurantId getRestaurantId() {
        return restaurantId;
    }
    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }
    public Money getPrice() {
        return price;
    }
    public List<OrderItem> getItems() {
        return items;
    }
    public TrackingId getTrackingId() {
        return trackingId;
    }
    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    public List<String> getFailureMessages() {
        return failureMessages;
    }

    // builderオブジェクトを生成するためのメソッドは一旦作成しないでおく。
    // なくても new OrderItem.Builder() でデフォルトコンストラクタを呼びオブジェクト生成はできる

    public static class Builder {
        private OrderId orderId;
        private CustomerId customerId;
        private RestaurantId restaurantId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        public Builder setOrderId(OrderId orderId) {
            this.orderId = orderId;
            return this;
        }
        public Builder setCustomerId(CustomerId customerId) {
            this.customerId = customerId;
            return this;
        }
        public Builder setRestaurantId(RestaurantId restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }
        public Builder setDeliveryAddress(StreetAddress deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }
        public Builder setPrice(Money price) {
            this.price = price;
            return this;
        }
        public Builder setItems(List<OrderItem> items) {
            this.items = items;
            return this;
        }
        public Builder setTrackingId(TrackingId trackingId) {
            this.trackingId = trackingId;
            return this;
        }
        public Builder setOrderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }
        public Builder setFailureMessages(List<String> failureMessages) {
            this.failureMessages = failureMessages;
            return this;
        }
        public Order buildObject() {
            return new Order(this);
        }
    } 
}
