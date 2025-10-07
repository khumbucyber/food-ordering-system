package com.food.ordering.system.payment.service.messaging.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;

/**
 * PaymentMessagingDataMapper
 * KafkaのAvroモデルとPaymentドメインモデル間のデータ変換を行うマッパー
 */
@Component
public class PaymentMessagingDataMapper {

    /**
     * PaymentRequestAvroModelをPaymentRequestに変換する
     * @param paymentRequestAvroModel Kafkaから受信したPaymentリクエストのAvroモデル
     * @return PaymentRequest ドメイン層で使用するPaymentリクエスト
     */
    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
            .id(paymentRequestAvroModel.getId())
            .sagaId(paymentRequestAvroModel.getSagaId())
            .orderId(paymentRequestAvroModel.getOrderId())
            .customerId(paymentRequestAvroModel.getCustomerId())
            .price(paymentRequestAvroModel.getPrice())
            .createdAt(paymentRequestAvroModel.getCreatedAt())
            .paymentOrderStatus(PaymentOrderStatus.valueOf(paymentRequestAvroModel.getPaymentOrderStatus().name()))
            .build();
    }

    /**
     * PaymentCompletedEventをPaymentResponseAvroModelに変換する
     * @param paymentCompletedEvent 支払い完了イベント
     * @return PaymentResponseAvroModel Kafkaに送信するPaymentレスポンスのAvroモデル
     */
    public PaymentResponseAvroModel paymentCompletedEventToPaymentResponseAvroModel(
            PaymentCompletedEvent paymentCompletedEvent) {
        return PaymentResponseAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setPaymentId(paymentCompletedEvent.getPayment().getId().getValue().toString())
            .setCustomerId(paymentCompletedEvent.getPayment().getCustomerId().getValue().toString())
            .setOrderId(paymentCompletedEvent.getPayment().getOrderId().getValue().toString())
            .setPrice(paymentCompletedEvent.getPayment().getPrice().getAmount())
            .setCreatedAt(paymentCompletedEvent.getCreatedAt().toInstant())
            .setPaymentStatus(PaymentStatus.COMPLETED)
            .setFailureMessage(java.util.Collections.emptyList())
            .build();
    }

    /**
     * PaymentCancelledEventをPaymentResponseAvroModelに変換する
     * @param paymentCancelledEvent 支払いキャンセルイベント
     * @return PaymentResponseAvroModel Kafkaに送信するPaymentレスポンスのAvroモデル
     */
    public PaymentResponseAvroModel paymentCancelledEventToPaymentResponseAvroModel(
            PaymentCancelledEvent paymentCancelledEvent) {
        return PaymentResponseAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setPaymentId(paymentCancelledEvent.getPayment().getId().getValue().toString())
            .setCustomerId(paymentCancelledEvent.getPayment().getCustomerId().getValue().toString())
            .setOrderId(paymentCancelledEvent.getPayment().getOrderId().getValue().toString())
            .setPrice(paymentCancelledEvent.getPayment().getPrice().getAmount())
            .setCreatedAt(paymentCancelledEvent.getCreatedAt().toInstant())
            .setPaymentStatus(PaymentStatus.CANCELLED)
            .setFailureMessage(java.util.Collections.emptyList())
            .build();
    }

    /**
     * PaymentFailedEventをPaymentResponseAvroModelに変換する
     * @param paymentFailedEvent 支払い失敗イベント
     * @return PaymentResponseAvroModel Kafkaに送信するPaymentレスポンスのAvroモデル
     */
    public PaymentResponseAvroModel paymentFailedEventToPaymentResponseAvroModel(
            PaymentFailedEvent paymentFailedEvent) {
        return PaymentResponseAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setPaymentId(paymentFailedEvent.getPayment().getId().getValue().toString())
            .setCustomerId(paymentFailedEvent.getPayment().getCustomerId().getValue().toString())
            .setOrderId(paymentFailedEvent.getPayment().getOrderId().getValue().toString())
            .setPrice(paymentFailedEvent.getPayment().getPrice().getAmount())
            .setCreatedAt(paymentFailedEvent.getCreatedAt().toInstant())
            .setPaymentStatus(PaymentStatus.FAILED)
            .setFailureMessage(paymentFailedEvent.getFailureMessages())
            .build();
    }
}
