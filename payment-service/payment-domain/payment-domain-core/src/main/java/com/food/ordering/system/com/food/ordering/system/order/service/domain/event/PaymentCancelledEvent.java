package com.food.ordering.system.com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.Payment;

public class PaymentCancelledEvent extends PaymentEvent{

    public PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt) {
        super(payment, createdAt, Collections.emptyList());
    }
}
