package com.food.ordering.system.restaurant.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * RestaurantApprovalRequestのKafkaリスナー
 * Order Serviceからの注文承認リクエストを受信
 */
@Slf4j
@Component
public class RestaurantApprovalRequestKafkaListener implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {

    private final RestaurantApprovalRequestMessageListener restaurantApprovalRequestMessageListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

    public RestaurantApprovalRequestKafkaListener(
            RestaurantApprovalRequestMessageListener restaurantApprovalRequestMessageListener,
            RestaurantMessagingDataMapper restaurantMessagingDataMapper) {
        this.restaurantApprovalRequestMessageListener = restaurantApprovalRequestMessageListener;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
    }

    /**
     * Kafkaメッセージを受信して処理
     * @param messages 受信したメッセージリスト
     * @param keys メッセージキーリスト
     * @param partitions パーティションリスト
     * @param offsets オフセットリスト
     */
    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
                   topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(@Payload List<RestaurantApprovalRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant approval requests received with keys: {}, partitions: {}, and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(restaurantApprovalRequestAvroModel -> {
            try {
                log.info("Processing restaurant approval for order id: {}", 
                         restaurantApprovalRequestAvroModel.getOrderId());
                this.restaurantApprovalRequestMessageListener.approveOrder(
                    this.restaurantMessagingDataMapper.restaurantApprovalRequestAvroModelToRestaurantApproval(
                        restaurantApprovalRequestAvroModel));
            } catch (RestaurantNotFoundException e) {
                log.error("No restaurant found for restaurant id: {}", 
                         restaurantApprovalRequestAvroModel.getRestaurantId());
            }
        });
    }
}
