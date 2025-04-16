package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.ValueObject.OrderApprovalStatus;
import com.food.ordering.system.domain.ValueObject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;

@Component
public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent) {
        Order order = orderCreatedEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setCustomerId(order.getCustomerId().getValue().toString())
            .setOrderId(order.getId().getValue().toString())
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
            .setPaymentOrderStatus((PaymentOrderStatus.PENDING))
            .build();
    }

    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(OrderCancelledEvent orderCancelledEvent) {
        Order order = orderCancelledEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setCustomerId(order.getCustomerId().getValue().toString())
            .setOrderId(order.getId().getValue().toString())
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
            .setPaymentOrderStatus((PaymentOrderStatus.PENDING))
            .build();
    }

    public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            // 次回のセクションでsagaパターンを実装する際に埋める。
            .setSagaId("")
            .setOrderId(order.getId().getValue().toString())
            .setRestaurantId(order.getRestaurantId().getValue().toString())
            .setOrderId(order.getId().getValue().toString())
            .setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(order.getOrderStatus().name()))
            // orderからproductsを1つ1つ取り出し、productのavroモデルのリストに変換する。
            // 変換の際にproductのavroモデルのbuilderを使う。
            // /home/khumbucyber/_projects/food-ordering-system/infrastructure/kafka/kafka-model/src/main/java/com/food/ordering/system/kafka/order/avro/model/Product.java
            .setProducts(order.getItems().stream().map(item ->
                Product.newBuilder()
                    .setId(item.getProduct().getId().getValue().toString())
                    .setQuantity(item.getQuantity())
                    .build()
                ).collect(Collectors.toList()))
            .setPrice(order.getPrice().getAmount())
            .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
            .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
            .build();
    }

    /*
     * PaymentResponseAvroModel ⇒ PaymentResponseの変換を行う
     * PaymentResponse の builder を使ってオブジェクト生成し返却する。
     * @param PaymentResponseAvroModel
     * @return PaymentResponse
     */
    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
            .id(paymentResponseAvroModel.getId())
            .sagaId(paymentResponseAvroModel.getSagaId())
            .orderId(paymentResponseAvroModel.getOrderId())
            .paymentId(paymentResponseAvroModel.getPaymentId())
            .customerId(paymentResponseAvroModel.getCustomerId())
            .price(paymentResponseAvroModel.getPrice())
            .createdAt(paymentResponseAvroModel.getCreatedAt())
            // TODO:構文の意味を理解する。
            .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
            .failureMessages(null)
            .build();
    }
    /*
     * RestaurantApprovalResponseAvroModel ⇒ RestaurantApprovalResponse の変換を行う
     * RestaurantApprovalResponse の builder を使ってオブジェクト生成し返却する。
     * @param RestaurantApprovalResponseAvroModel
     * @return RestaurantApprovalResponse
     */
    public RestaurantApprovalResponse restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
            .id(restaurantApprovalResponseAvroModel.getId())
            .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
            .orderId(restaurantApprovalResponseAvroModel.getOrderId())
            .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
            .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
            .orderApprovalStatus(OrderApprovalStatus.valueOf(restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
            .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessage())
            .build();
    }
}
