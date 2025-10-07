package com.food.ordering.system.payment.service.domain.ports.input.message.listner;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;

public interface PaymentRequestMessageListner {

    void completePayment(PaymentRequest paymentRequest);

    void cancelPayment(PaymentRequest paymentRequest);
}
