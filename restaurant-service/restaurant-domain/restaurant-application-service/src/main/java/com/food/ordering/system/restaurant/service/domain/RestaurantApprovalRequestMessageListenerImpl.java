package com.food.ordering.system.restaurant.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 注文承認リクエストのメッセージリスナー実装クラス
 * Order Serviceからのメッセージを受信し、承認処理を実行する
 */
@Slf4j
@Service
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {

    private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;

    public RestaurantApprovalRequestMessageListenerImpl(RestaurantApprovalRequestHelper restaurantApprovalRequestHelper) {
        this.restaurantApprovalRequestHelper = restaurantApprovalRequestHelper;
    }

    /**
     * 注文の承認処理を実行
     * @param restaurantApprovalRequest 注文承認リクエスト
     */
    @Override
    public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        this.restaurantApprovalRequestHelper.persistOrderApproval(restaurantApprovalRequest);
    }
}
