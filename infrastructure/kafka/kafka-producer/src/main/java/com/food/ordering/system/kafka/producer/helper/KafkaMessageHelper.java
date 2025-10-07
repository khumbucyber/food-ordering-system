package com.food.ordering.system.kafka.producer.helper;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

/**
 * KafkaMessageHelper
 * Kafkaメッセージ送信時のコールバック処理を提供する共通ヘルパークラス
 */
@Slf4j
@Component
public class KafkaMessageHelper {

    /**
     * Kafkaメッセージ送信のコールバックを取得する
     * @param <T> Avroモデルの型
     * @param responseTopicName レスポンストピック名
     * @param avroModel Avroモデル
     * @param orderId 注文ID
     * @param avroModelName Avroモデル名
     * @return ListenableFutureCallback Kafkaメッセージ送信のコールバック
     */
    public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
            String responseTopicName, T avroModel, String orderId, String avroModelName) {
        return new ListenableFutureCallback<SendResult<String, T>>() {

            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending {} message {} to topic {}",
                    avroModelName, avroModel.toString(), responseTopicName, ex);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received successful response from kafka for order id: {}" +
                    " Topic: {} Partition: {} Offset: {} Timestamp: {}",
                    orderId,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp()
                );
            }
        };
    }
}
