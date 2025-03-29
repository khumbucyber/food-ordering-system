package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKeyBottom;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaProducerConfig;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher{

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

    public CreateOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
            OrderServiceConfigData orderServiceConfigData,
            KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void publish(OrderCreatedEvent orderCreatedEvent) {
        String orderId = orderCreatedEvent.getOrder().getId().toString();
        log.info("Received OrderCreatedEvent for order id: {}", orderId);

        try {
            PaymentRequestAvroModel paymentRequestAvroModel =
                orderMessagingDataMapper.orderCreatedEventToPaymentRequestAvroModel(orderCreatedEvent);

            kafkaProducer.send(
                orderServiceConfigData.getPaymentRequestTopicName(), 
                orderId, 
                paymentRequestAvroModel, 
                getKafkaCallback(orderServiceConfigData.getPaymentResponseTopicName(), paymentRequestAvroModel)
            );

            log.info("PaymentRequestAvroModel sent to kafka for order id: {}",
                paymentRequestAvroModel.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending PaymentRequestAvroModel message" +
                " to kafka with order id: {}, error: {}", orderId, e.getMessage()
            );
        }
    }

    private ListenableFutureCallback<SendResult<String, PaymentRequestAvroModel>> getKafkaCallback(
            String paymentResponseTopicName, PaymentRequestAvroModel paymentRequestAvroModel) {
        return new ListenableFutureCallback<SendResult<String,PaymentRequestAvroModel>>() {

            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending PaymentRequestAvroModel" +
                    " message {} to topic {}", paymentRequestAvroModel.toString(), paymentResponseTopicName, ex);
            }

            @Override
            public void onSuccess(SendResult<String, PaymentRequestAvroModel> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received successful response from kafka for order id: {}" +
                    " topic: {} Partition: {} Offset: {} Timestamp: {}",
                    paymentRequestAvroModel.getOrderId(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp()
                );
            }
        };
    }
}
