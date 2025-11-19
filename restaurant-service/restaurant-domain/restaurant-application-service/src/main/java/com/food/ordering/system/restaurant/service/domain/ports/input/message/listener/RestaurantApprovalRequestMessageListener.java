package com.food.ordering.system.restaurant.service.domain.ports.input.message.listener;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;

/**
 * 注文承認リクエストを処理する入力ポート
 */
public interface RestaurantApprovalRequestMessageListener {

    /**
     * 注文の承認処理を実行
     * @param restaurantApprovalRequest 注文承認リクエスト
     */
    void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}
