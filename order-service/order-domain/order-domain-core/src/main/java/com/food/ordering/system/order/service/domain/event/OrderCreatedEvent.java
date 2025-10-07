package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.order.service.domain.entity.Order;

public class OrderCreatedEvent extends OrderEvent {
    public OrderCreatedEvent(Order order, ZonedDateTime createdAt) {
        super(order, createdAt);
    }

    @Override
    public void fire() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fire'");
    }
}
