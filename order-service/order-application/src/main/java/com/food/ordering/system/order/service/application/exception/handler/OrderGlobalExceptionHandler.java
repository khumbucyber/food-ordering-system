package com.food.ordering.system.order.service.application.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.food.ordering.system.applcation.handler.ErrorDTO;
import com.food.ordering.system.applcation.handler.GlobalExceptionHandler;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class OrderGlobalExceptionHandler extends GlobalExceptionHandler{

    @ResponseBody
    @ExceptionHandler(value = {OrderDomainException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO hadndlerException(OrderDomainException orderDomainException) {
        log.error(null, orderDomainException);
        return ErrorDTO.builder()
            .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(orderDomainException.getMessage())
            .build();
    }

    @ResponseBody
    @ExceptionHandler(value = {OrderNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO hadndlerException(OrderNotFoundException orderNotFoundException) {
        log.error(null, orderNotFoundException);
        return ErrorDTO.builder()
            .code(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(orderNotFoundException.getMessage())
            .build();
    }

}
