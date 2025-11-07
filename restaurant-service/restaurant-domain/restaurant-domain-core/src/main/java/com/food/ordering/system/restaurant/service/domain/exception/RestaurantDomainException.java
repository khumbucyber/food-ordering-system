package com.food.ordering.system.restaurant.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class RestaurantDomainException extends DomainException {

    public (String message) {
        super(message);
    }

    public RestaurantDomainException(String message, Throwable cause) {
        super(RestaurantDomainExceptionmessage, cause);
    }
}
