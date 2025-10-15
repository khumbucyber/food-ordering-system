package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
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

    public static final String FAILURE_MESSAGE_DELIMITER = ",";

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

    public void initCancel(List<String> failureMessages) {
        if (orderStatus != OrderStatus.PAID) {
            throw new OrderDomainException("Order is not in correct state for initCancel operation");
        }
        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessage(failureMessages);
    }
        
    public void cancel(List<String> failureMessages) {
        if (!(orderStatus == OrderStatus.CANCELLING || orderStatus == OrderStatus.PENDING)) {
            throw new OrderDomainException("Order is not in correct state for cancel operation");
        }
        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessage(failureMessages);
    }
    
    private void updateFailureMessage(List<String> failureMessages) {
        if (this.failureMessages != null && failureMessages != null) {
            this.failureMessages.addAll(failureMessages.stream().filter(message -> !message.isEmpty()).toList());
        }
        if (this.failureMessages == null) {
            this.failureMessages = failureMessages;
        }
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

        if (!price.equals(orderItemsTotal)) {
            throw new OrderDomainException("Total Price: " + price.getAmount()
                + " is not equal to Order items total: " + orderItemsTotal.getAmount() + " !");
        }
    }

    private void validateItemPrice(OrderItem orderItem) {
        // Udemy-25のtest class作成の途中(3:43)出てきたが、実装されていなかったので実装した
        // Udemy-15 9:30～を見て実装
        if (!orderItem.isPriceValid()) {
            throw new OrderDomainException("Order item price: " + orderItem.getPrice().getAmount() +
                " is not valid for product " + orderItem.getProduct().getId().getValue());
        }
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
    // なくても new Order.Builder() でデフォルトコンストラクタを呼びオブジェクト生成はできる
    // ↓やっぱり書いた
    // 内部クラスをnewするメソッド
    // これを最初書かなかった。これ要るの？
    // まあこれがあった方が、使い側は簡潔に書けるので便利だ。
    // staticな内部クラスなので、外部クラスのインスタンスを生成しなくても
    // 内部クラスのインスタンスを生成できる。
    // https://chatgpt.com/share/66e96836-6ce4-800e-b011-d42555f3ca7a
    public static Builder builder() {
        return new Builder();
    }

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
        public Order build() {
            return new Order(this);
        }
    } 
}
