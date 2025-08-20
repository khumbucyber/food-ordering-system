package com.food.ordering.system.payment.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.port.input.message.listner.PaymentRequestMessageListner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentRequestMessageListnerImpl implements PaymentRequestMessageListner{@Override
    public void completePayment(PaymentRequest paymentRequest) {
    }

    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
    }

}
