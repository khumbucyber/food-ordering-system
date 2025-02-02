package com.food.ordering.system.order.service.domain.ports.input.message.listner.restaurantapproval;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
public class RestaurantApprovalResponseMessageListnerImpl 
    implements RestaurantApprovalResponseMessageListner{

    @Override
    public void orderApproval(RestaurantApprovalResponse restaurantApprovalResponse) {
        
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
        
    }

}
