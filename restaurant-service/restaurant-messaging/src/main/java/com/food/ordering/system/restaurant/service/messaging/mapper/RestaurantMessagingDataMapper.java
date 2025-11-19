package com.food.ordering.system.restaurant.service.messaging.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;

/**
 * Avroモデル⇔DTO変換を行うマッパー
 */
@Component
public class RestaurantMessagingDataMapper {

    /**
     * RestaurantApprovalRequestAvroModelをRestaurantApprovalRequestに変換
     * @param restaurantApprovalRequestAvroModel Avroモデル
     * @return RestaurantApprovalRequest DTO
     */
    public RestaurantApprovalRequest restaurantApprovalRequestAvroModelToRestaurantApproval(
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel) {
        return RestaurantApprovalRequest.builder()
            .id(restaurantApprovalRequestAvroModel.getId())
            .sagaId(restaurantApprovalRequestAvroModel.getSagaId())
            .restaurantId(restaurantApprovalRequestAvroModel.getRestaurantId())
            .orderId(restaurantApprovalRequestAvroModel.getOrderId())
            .restaurantOrderStatus(com.food.ordering.system.restaurant.service.domain.dto.RestaurantOrderStatus
                .valueOf(restaurantApprovalRequestAvroModel.getRestaurantOrderStatus().name()))
            .products(restaurantApprovalRequestAvroModel.getProducts().stream()
                .map(avroProduct -> com.food.ordering.system.restaurant.service.domain.dto.Product.builder()
                    .id(avroProduct.getId())
                    .quantity(avroProduct.getQuantity())
                    .build())
                .collect(Collectors.toList()))
            .price(restaurantApprovalRequestAvroModel.getPrice())
            .createdAt(restaurantApprovalRequestAvroModel.getCreatedAt())
            .build();
    }

    /**
     * OrderApprovedEventをRestaurantApprovalResponseAvroModelに変換
     * @param orderApprovedEvent 承認イベント
     * @return RestaurantApprovalResponseAvroModel Avroモデル
     */
    public RestaurantApprovalResponseAvroModel orderApprovedEventToRestaurantApprovalResponseAvroModel(
            OrderApprovedEvent orderApprovedEvent) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setRestaurantId(orderApprovedEvent.getRestaurantId().getValue().toString())
            .setOrderId(orderApprovedEvent.getOrderApproval().getOrderId().getValue().toString())
            .setCreatedAt(orderApprovedEvent.getCreatedAt().toInstant())
            .setOrderApprovalStatus(OrderApprovalStatus.APPROVED)
            .setFailureMessage(orderApprovedEvent.getFailureMessages())
            .build();
    }

    /**
     * OrderRejectedEventをRestaurantApprovalResponseAvroModelに変換
     * @param orderRejectedEvent 拒否イベント
     * @return RestaurantApprovalResponseAvroModel Avroモデル
     */
    public RestaurantApprovalResponseAvroModel orderRejectedEventToRestaurantApprovalResponseAvroModel(
            OrderRejectedEvent orderRejectedEvent) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSagaId("")
            .setRestaurantId(orderRejectedEvent.getRestaurantId().getValue().toString())
            .setOrderId(orderRejectedEvent.getOrderApproval().getOrderId().getValue().toString())
            .setCreatedAt(orderRejectedEvent.getCreatedAt().toInstant())
            .setOrderApprovalStatus(OrderApprovalStatus.REJECTED)
            .setFailureMessage(orderRejectedEvent.getFailureMessages())
            .build();
    }

    /**
     * OrderApprovalEventをRestaurantApprovalResponseAvroModelに変換
     * イベントの型に応じて適切な変換メソッドを呼び出す
     * @param orderApprovalEvent 承認イベント（OrderApprovedEventまたはOrderRejectedEvent）
     * @return RestaurantApprovalResponseAvroModel Avroモデル
     */
    public RestaurantApprovalResponseAvroModel orderApprovalEventToRestaurantApprovalResponseAvroModel(
            OrderApprovalEvent orderApprovalEvent) {
        if (orderApprovalEvent instanceof OrderApprovedEvent) {
            return this.orderApprovedEventToRestaurantApprovalResponseAvroModel(
                (OrderApprovedEvent) orderApprovalEvent);
        } else {
            return this.orderRejectedEventToRestaurantApprovalResponseAvroModel(
                (OrderRejectedEvent) orderApprovalEvent);
        }
    }
}
