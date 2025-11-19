package com.food.ordering.system.restaurant.service.domain.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.valueobject.ProductId;

/**
 * DTO⇔ドメインエンティティ変換を行うマッパークラス
 */
@Component
public class RestaurantDataMapper {

    /**
     * RestaurantApprovalRequestをRestaurantドメインオブジェクトに変換
     * @param restaurantApprovalRequest 注文承認リクエストDTO
     * @return Restaurantドメインオブジェクト
     */
    public Restaurant restaurantApprovalRequestToRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        return Restaurant.builder()
            .restaurantId(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getRestaurantId())))
            .orderDetail(OrderDetail.builder()
                .orderId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
                .products(this.productsToProducts(restaurantApprovalRequest.getProducts()))
                .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
                .orderStatus(OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
                .build())
            .build();
    }

    /**
     * DTOのProductリストをドメインのProductリストに変換
     * @param products DTOのProductリスト
     * @return ドメインのProductリスト
     */
    private List<Product> productsToProducts(List<com.food.ordering.system.restaurant.service.domain.dto.Product> products) {
        return products.stream()
            .map(product -> Product.builder()
                .productId(new ProductId(UUID.fromString(product.getId())))
                .quantity(product.getQuantity())
                .build())
            .collect(Collectors.toList());
    }
}
