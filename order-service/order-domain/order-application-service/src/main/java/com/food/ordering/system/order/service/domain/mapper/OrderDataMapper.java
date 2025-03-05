package com.food.ordering.system.order.service.domain.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.ProductId;
import com.food.ordering.system.domain.ValueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddressDto;
// dtoとvalueobjectで同一のファイル名OrderItemとしているため、2つを同時にimportすることができない。
// https://chatgpt.com/share/67838b7a-3f60-800e-9ecd-19823d00d652
import com.food.ordering.system.order.service.domain.dto.create.OrderItemDto;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;

@Component
public class OrderDataMapper {
    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
        // streamを含む解説
        //   https://chatgpt.com/share/6781d1a8-3ac4-800e-81f4-9f1e2a729aa9
        return Restaurant.builder()
            .setRestaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
            // 動画では「new Product(new ProductId(orderItem.getProductId()」
            // ここでは「new Product(orderItem.getProduct().get()」
            // 合っているかわからない。
            .setProducts(createOrderCommand.getItems().stream().map(
                orderItemDto -> new Product(new ProductId(orderItemDto.getProductId())))
            .collect(Collectors.toList()))
            .build();
    }

    public Order creatOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
            .setCustomerId(new CustomerId(createOrderCommand.getCustomerId()))
            .setRestaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
            .setDeliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
            .setPrice(new Money(createOrderCommand.getPrice()))
            .setItems(orderItemToOrderItemEntities(createOrderCommand.getItems()))
            .build();
    }

    public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
            .orderTrackingId(order.getTrackingId().getValue())
            .orderStatus(order.getOrderStatus())
            .message(message)
            .build();
    }

    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
            .orderTrackingId(order.getTrackingId().getValue())
            .orderStatus(order.getOrderStatus())
            .failureMessages(order.getFailureMessages())
            .build();
    }

    private StreetAddress orderAddressToStreetAddress(OrderAddressDto orderAddressDto) {
        return new StreetAddress(
            UUID.randomUUID(),
            orderAddressDto.getStreet(),
            orderAddressDto.getPostalCode(),
            orderAddressDto.getCity()
        );
    }

    private List<OrderItem> orderItemToOrderItemEntities(List<OrderItemDto> orderItems) {
        return orderItems.stream()
            .map(orderItemDto ->  
                OrderItem.builder()    
                    .setProduct(new Product(new ProductId(orderItemDto.getProductId())))
                    .setPrice(new Money(orderItemDto.getPrice()))
                    .setQuantity(orderItemDto.getQuantity())
                    .setSubTotal(new Money(orderItemDto.getSubTotal()))
                    .build()).collect(Collectors.toList()); // ここの理解要
    }
}