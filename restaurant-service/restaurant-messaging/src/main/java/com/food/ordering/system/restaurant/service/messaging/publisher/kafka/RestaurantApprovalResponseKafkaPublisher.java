package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.helper.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * RestaurantApprovalResponseのKafkaパブリッシャー
 * Order Serviceへ承認結果を送信
 */
@Slf4j
@Component
public class RestaurantApprovalResponseKafkaPublisher implements
        RestaurantApprovalResponseMessagePublisher {

    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final RestaurantServiceConfigData restaurantServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantApprovalResponseKafkaPublisher(
            RestaurantMessagingDataMapper restaurantMessagingDataMapper,
            RestaurantServiceConfigData restaurantServiceConfigData,
            KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer,
            KafkaMessageHelper kafkaMessageHelper) {
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
        this.restaurantServiceConfigData = restaurantServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    /**
     * 承認結果イベントを発行
     * @param orderApprovalEvent 承認または拒否イベント
     */
    @Override
    public void publish(OrderApprovalEvent orderApprovalEvent) {
        String orderId = orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString();

        log.info("Received OrderApprovalEvent for order id: {} and restaurant id: {}",
                 orderId,
                 orderApprovalEvent.getRestaurantId().getValue());

        try {
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel =
                this.restaurantMessagingDataMapper.orderApprovalEventToRestaurantApprovalResponseAvroModel(
                    orderApprovalEvent);

            this.kafkaProducer.send(
                this.restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                orderId,
                restaurantApprovalResponseAvroModel,
                this.kafkaMessageHelper.getKafkaCallback(
                    this.restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                    restaurantApprovalResponseAvroModel,
                    orderId,
                    "RestaurantApprovalResponseAvroModel"
                )
            );

            log.info("RestaurantApprovalResponseAvroModel sent to kafka for order id: {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message" +
                " to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}
