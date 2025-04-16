package com.food.ordering.system.order.service.domain.ports.input.message.listner.payment;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
public class PaymentResponseMessageListnerImpl implements PaymentResponseMessageListner{

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        
    }
}
