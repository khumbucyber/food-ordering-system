package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrdrePaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

/*
 * PayOrderKafkaMessagePublisher
 * 
 */
@Slf4j
@Component
public class PayOrderKafkaMessagePublisher implements OrdrePaidRestaurantRequestMessagePublisher{
    
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderKafkaMessageHelper orderKafkaMessageHelper;
    
    public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
            OrderServiceConfigData orderServiceConfigData,
            KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
            OrderKafkaMessageHelper orderKafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }

    /*
     * OrderPaidEventオブジェクトをAvroオブジェクトに変換し、Kafkaへ送信する。
     * @param OrderPaidEvent
     */
    @Override
    public void publish(OrderPaidEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        
        try {
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel =
                orderMessagingDataMapper.orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);
            
            kafkaProducer.send(
                orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                orderId,
                restaurantApprovalRequestAvroModel,
                orderKafkaMessageHelper.getKafkaCallback(
                    orderServiceConfigData.getRestaurantApprovalRequestTopicName(), 
                    restaurantApprovalRequestAvroModel,
                    orderId,
                    "RestaurantApprovalRequestAvroModel"
                )
            );
            log.info("RestaurantApprovalRequestAvroModel sent to kafka for order id: {}",
                restaurantApprovalRequestAvroModel.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalRequestAvroModel message" +
                " to kafka with order id: {}, error: {}", orderId, e.getMessage()
            );
        }
    }
}