package com.food.ordering.system.payment.service.domain.mapper;

import java.util.UUID;

import org.springframework.core.annotation.Order;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.OrderId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;

/*
 * 入力オブジェクトからドメインオブジェクトへの変換や
 * ドメインオブジェクトから出力オブジェクトへの変換を行うマッパークラス
 */
public class PaymentDataMapper {

    public Payment paymentRequestToPayment(PaymentRequest paymentRequest) {
        return Payment.builder()
                .orderId(new OrderId(UUID.fromString(paymentRequest.getOrderId())))
                .customerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
                .price(new Money(paymentRequest.getPrice()))
                .build();
    }
}
