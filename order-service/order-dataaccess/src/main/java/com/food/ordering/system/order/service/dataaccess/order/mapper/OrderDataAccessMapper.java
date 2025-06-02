package com.food.ordering.system.order.service.dataaccess.order.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.OrderId;
import com.food.ordering.system.domain.ValueObject.ProductId;
import com.food.ordering.system.domain.ValueObject.RestaurantId;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

@Component
public class OrderDataAccessMapper {

    // order-domain-coreのOrderエンティティをOrderEntityオブジェクトに変換する。
    // そういえば、OrderEntityの各種IDは、それぞれのValueObjectのTypeではなく純粋なUUID Typeになっているんだ。
    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
            .id(order.getId().getValue())
            .customerId(order.getCustomerId().getValue())
            .restaurantId(order.getRestaurantId().getValue())
            .trackingId(order.getTrackingId().getValue())
            .address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
            .price(order.getPrice().getAmount())
            .items(orderItemsToOrderItemEntities(order.getItems()))
            .orderStatus(order.getOrderStatus())
            .failureMessage(order.getFailureMessages() != null ? 
                String.join(Order.FAILURE_MESSAGE_DELIMITER, order.getFailureMessages()) : "")
            .build();
            // 作成したOrderEntityオブジェクトを、そのOrderEntityのAddressのOrderEntityにセットする。
            orderEntity.getAddress().setOrderEntity(orderEntity);
            // 作成したOrderEntityオブジェクトを、そのOrderEntityのItemListのOrderEntityにセットする。
            orderEntity.getItems().forEach(orderItemEntity -> orderItemEntity.setOrderEntity(orderEntity));

        return orderEntity;
    }

    // OrderEntityオブジェクトからOrderオブジェクトに変換する。
    // Orderオブジェクトの各種IDは、UUIDではなくそれぞれのValueObjectタイプであることに注意
    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.builder()
            .setOrderId(new OrderId(orderEntity.getId()))
            .setCustomerId(new CustomerId(orderEntity.getCustomerId()))
            .setRestaurantId(new RestaurantId(orderEntity.getRestaurantId()))
            .setDeliveryAddress(AddressEntityToDeliveryAddress(orderEntity.getAddress()))
            .setPrice(new Money(orderEntity.getPrice()))
            .setItems(orderItemEntitiesToOrderItems(orderEntity.getItems()))
            .setTrackingId(new TrackingId(orderEntity.getTrackingId()))
            .setOrderStatus(orderEntity.getOrderStatus())
            // カンマ区切りでつながったStringをListに変換する。
            .setFailureMessages(orderEntity.getFailureMessage().isEmpty() ? new ArrayList<>() :
                new ArrayList<>(Arrays.asList(orderEntity.getFailureMessage()
                    .split(Order.FAILURE_MESSAGE_DELIMITER))))
            .build();
        }
    
    // StreetAddressオブジェクトを、DB登録用にOrderAddressEntityオブジェクトに詰め替える
    private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress deliveryAddress) {
        return OrderAddressEntity.builder()
            .id(deliveryAddress.getId())
            .street(deliveryAddress.getStreet())
            .postalCode(deliveryAddress.getPostalCode())
            .city(deliveryAddress.getCity())
            .build();
    }

    // DB登録されたOrderAddressEntityオブジェクトをStreetAddressオブジェクトに詰め替える
    private StreetAddress AddressEntityToDeliveryAddress(OrderAddressEntity orderAddressEntity) {
        return new StreetAddress(
                orderAddressEntity.getId(), orderAddressEntity.getStreet(),
                orderAddressEntity.getPostalCode(), orderAddressEntity.getCity());
    }

    // OrderItemのListオブジェクトを、DB登録用にOrderItemEntityのListオブジェクトに詰め替える
    private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
        return items.stream()
            .map(orderItem -> OrderItemEntity.builder()
                .id(orderItem.getId().getValue())
                .productId(orderItem.getProduct().getId().getValue())
                .price(orderItem.getPrice().getAmount())
                .quantity(orderItem.getQuantity())
                .subTotal(orderItem.getSubTotal().getAmount())
                .build()
            ).collect(Collectors.toList());
    }

    // DB登録されたOrderItemEntityのListオブジェクトを、OrderItemのListに詰め替える
    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
        return items.stream()
            .map(orderItemEntity -> OrderItem.builder()
                .orderItemId(new OrderItemId(orderItemEntity.getId()))
                // productIdのセットだけではだめだろう。
                // でもOrderItemEntityはproductIdしか持ってないのでどこから他のカラム情報をとってくるのか？
                .product(new Product(new ProductId(orderItemEntity.getProductId())))
                .price(new Money(orderItemEntity.getPrice()))
                .quantity(orderItemEntity.getQuantity())
                .subTotal(new Money(orderItemEntity.getSubTotal()))
                .build()
            ).collect(Collectors.toList());
    }
}