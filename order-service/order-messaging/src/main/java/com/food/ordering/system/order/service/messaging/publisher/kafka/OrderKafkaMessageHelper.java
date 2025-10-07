package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.food.ordering.system.kafka.producer.helper.KafkaMessageHelper;

/**
 * OrderKafkaMessageHelper
 * 共通のKafkaMessageHelperへの委譲クラス
 * @deprecated 共通モジュールのKafkaMessageHelperを直接使用してください
 */
@Component
@Deprecated
public class OrderKafkaMessageHelper {

    private final KafkaMessageHelper kafkaMessageHelper;

    public OrderKafkaMessageHelper(KafkaMessageHelper kafkaMessageHelper) {
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
            String responseTopicName, T requestAvroModel, String orderId, String requestAvroModelName) {
        return kafkaMessageHelper.getKafkaCallback(responseTopicName, requestAvroModel, orderId, requestAvroModelName);
    }
}
