package com.food.ordering.system.payment.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class PaymentAppicationServiceException extends DomainException{

    public PaymentAppicationServiceException(String message) {
        super(message);
    }

    public PaymentAppicationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}