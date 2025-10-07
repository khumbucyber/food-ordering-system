package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.helper.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * PaymentCompletedKafkaMessagePublisher
 * Payment完了イベントをKafkaに発行するパブリッシャー
 */
@Slf4j
@Component
public class PaymentCompletedKafkaMessagePublisher implements PaymentCompletedMessagePublisher {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final PaymentServiceConfigData paymentServiceConfigData;
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final KafkaMessageHelper kafkaMessageHelper;

    public PaymentCompletedKafkaMessagePublisher(PaymentMessagingDataMapper paymentMessagingDataMapper,
            PaymentServiceConfigData paymentServiceConfigData,
            KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer,
            KafkaMessageHelper kafkaMessageHelper) {
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
        this.paymentServiceConfigData = paymentServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(PaymentCompletedEvent domainEvent) {
        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

        log.info("Received PaymentCompletedEvent for order id: {}", orderId);

        try {
            PaymentResponseAvroModel paymentResponseAvroModel =
                paymentMessagingDataMapper.paymentCompletedEventToPaymentResponseAvroModel(domainEvent);

            kafkaProducer.send(
                paymentServiceConfigData.getPaymentResponseTopicName(),
                orderId,
                paymentResponseAvroModel,
                // sendメソッドは非同期なので、コールバックを指定して送信結果を処理する
                kafkaMessageHelper.getKafkaCallback(
                    paymentServiceConfigData.getPaymentResponseTopicName(),
                    paymentResponseAvroModel,
                    orderId,
                    "PaymentResponseAvroModel"
                )
            );

            log.info("PaymentResponseAvroModel sent to kafka for order id: {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending PaymentResponseAvroModel message" +
                " to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}
