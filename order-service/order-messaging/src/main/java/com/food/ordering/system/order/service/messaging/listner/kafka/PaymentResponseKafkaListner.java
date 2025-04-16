package com.food.ordering.system.order.service.messaging.listner.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.order.service.domain.ports.input.message.listner.payment.PaymentResponseMessageListner;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentResponseKafkaListner implements KafkaConsumer<PaymentResponseAvroModel> {

    private final PaymentResponseMessageListner paymentResponseMessageListner;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    
    public PaymentResponseKafkaListner(PaymentResponseMessageListner paymentResponseMessageListner,
            OrderMessagingDataMapper orderMessagingDataMapper) {
        this.paymentResponseMessageListner = paymentResponseMessageListner;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}", 
                    topics = "${order-service.payment-response-topic-name}")
    public void receive(
                        // これらのアノテーションはパラメータをKafkaからの正しい値にマップする
                        @Payload List<PaymentResponseAvroModel> messages, 
                        // Key should be String not long(Udemy-39_10:00)
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys, 
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment responses received with keys:{}, partition:{}, and offset:{}",
                            messages.size(),
                            keys.toString(),
                            partitions.toString(),
                            offsets.toString());
        messages.forEach(paymentResponsAvroModel -> {
            if (PaymentStatus.COMPLETED == paymentResponsAvroModel.getPaymentStatus()) {
                log.info("Processing successful payment for order id: {}", paymentResponsAvroModel.getOrderId());
                paymentResponseMessageListner.paymentCompleted(
                    orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentResponsAvroModel));
            } else if (PaymentStatus.CANCELLED == paymentResponsAvroModel.getPaymentStatus() ||
                        PaymentStatus.FAILED == paymentResponsAvroModel.getPaymentStatus()) {
                log.info("Processing unsuccessful payment for order id: {}", paymentResponsAvroModel.getOrderId());
                paymentResponseMessageListner.paymentCancelled(
                    orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentResponsAvroModel));
            }
        });
        
    }

}
