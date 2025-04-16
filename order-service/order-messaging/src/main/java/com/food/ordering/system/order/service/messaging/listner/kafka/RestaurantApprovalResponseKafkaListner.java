package com.food.ordering.system.order.service.messaging.listner.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.ports.input.message.listner.restaurantapproval.RestaurantApprovalResponseMessageListner;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RestaurantApprovalResponseKafkaListner implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

    private final RestaurantApprovalResponseMessageListner restaurantApprovalResponseMessageListner;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantApprovalResponseKafkaListner(
            RestaurantApprovalResponseMessageListner restaurantApprovalResponseMessageListner,
            OrderMessagingDataMapper orderMessagingDataMapper) {
        this.restaurantApprovalResponseMessageListner = restaurantApprovalResponseMessageListner;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    /*
     * Kafkaからメッセージ(Avro形式)を受け取り、ドメイン層のListnerに受け渡す。
     */
    @Override
    @KafkaListener(id = "@{kafka-consumer-config.restaurant-approval-consumer-group-id}", 
                    topics = "${order-service.restaurant-approval-response-topic-name}")
    public void receive(
                        @Payload List<RestaurantApprovalResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant approval responses received with keys {}, partition {}, offset {}",
            messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        messages.forEach(restaurantApprovalResponseAvroModel -> {
            if (OrderApprovalStatus.APPROVED == restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
                log.info("Processing approved order for order id: {}", restaurantApprovalResponseAvroModel.getOrderId());
                restaurantApprovalResponseMessageListner.orderApproval(
                    orderMessagingDataMapper.restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
                        restaurantApprovalResponseAvroModel));
            } else if (OrderApprovalStatus.APPROVED == restaurantApprovalResponseAvroModel.getOrderApprovalStatus()) {
                log.info("Processing ", partitions);
            }
        });
    }
}
