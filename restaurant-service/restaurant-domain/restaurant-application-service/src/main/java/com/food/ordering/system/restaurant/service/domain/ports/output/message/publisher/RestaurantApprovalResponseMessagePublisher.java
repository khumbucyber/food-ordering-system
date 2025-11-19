package com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher;

import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;

/**
 * 承認結果をKafkaへ送信する出力ポート
 */
public interface RestaurantApprovalResponseMessagePublisher {

    /**
     * 承認結果イベントを発行
     * @param orderApprovalEvent 承認または拒否イベント
     */
    void publish(OrderApprovalEvent orderApprovalEvent);
}
