package com.food.ordering.system.payment.service.domain.port.output.repository;

import java.util.Optional;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(String orderId);
}
