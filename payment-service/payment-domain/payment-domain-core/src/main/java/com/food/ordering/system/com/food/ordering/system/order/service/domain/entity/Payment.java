package com.food.ordering.system.com.food.ordering.system.order.service.domain.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject.PaymentId;
import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.OrderId;
import com.food.ordering.system.domain.ValueObject.PaymentStatus;
import com.food.ordering.system.domain.entity.AggregateRoot;

public class Payment extends AggregateRoot<PaymentId>{

    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money price;
    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    private Payment(Builder builder) {
        super.setId(builder.paymentId);
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.price = builder.price;
        this.paymentStatus = builder.paymentStatus;
        this.createdAt = builder.createdAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }
    public CustomerId getCustomerId() {
        return customerId;
    }
    public Money getPrice() {
        return price;
    }
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void initializePayment() {
        setId(new PaymentId(UUID.randomUUID()));
        createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public void validatePayment(List<String> failureMessages) {
        if (price == null || !price.isGreaterThanZero()) {
            failureMessages.add("Total price must be greater than zero!");
        }
    }

    public void updateStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PaymentId paymentId;
        private OrderId orderId;
        private CustomerId customerId;
        private Money price;
        private PaymentStatus paymentStatus;
        private ZonedDateTime createdAt;

        public Builder paymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }
        public Builder orderId(OrderId orderId) {
            this.orderId = orderId;
            return this;
        }
        public Builder customerId(CustomerId customerId) {
            this.customerId = customerId;
            return this;
        }
        public Builder price(Money price) {
            this.price = price;
            return this;
        }
        public Builder paymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }
        public Builder createdAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public Payment build() {
            return new Payment(this);
        }
    }
}
