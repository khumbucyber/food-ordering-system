package com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

/**
 * ドメインエンティティ⇔JPAエンティティ変換マッパー
 */
@Component
public class RestaurantDataAccessMapper {

    /**
     * Restaurantドメインから商品IDリストを抽出
     * @param restaurant レストランドメインオブジェクト
     * @return 商品IDのUUIDリスト
     */
    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        return restaurant.getOrderDetail().getProducts().stream()
            .map(product -> product.getId().getValue())
            .collect(Collectors.toList());
    }

    /**
     * RestaurantEntityリストからRestaurantドメインオブジェクトに変換
     * @param restaurantEntities RestaurantEntityリスト
     * @return Restaurantドメインオブジェクト
     */
    public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity restaurantEntity = 
            restaurantEntities.stream().findFirst().orElseThrow(() ->
                new RestaurantDataAccessException("Restaurant could not be found!"));

        List<Product> restaurantProducts = restaurantEntities.stream().map(entity ->
            Product.builder()
                .productId(new ProductId(entity.getProductId()))
                .name(entity.getProductName())
                .price(new Money(entity.getProductPrice()))
                .available(entity.getProductAvailable())
                .build())
            .collect(Collectors.toList());

        return Restaurant.builder()
            .restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
            .orderDetail(OrderDetail.builder()
                .products(restaurantProducts)
                .build())
            .active(restaurantEntity.getRestaurantActive())
            .build();
    }

    /**
     * OrderApprovalドメインをOrderApprovalEntityに変換
     * @param orderApproval 注文承認ドメインオブジェクト
     * @return OrderApprovalEntity
     */
    public OrderApprovalEntity orderApprovalToOrderApprovalEntity(OrderApproval orderApproval) {
        return OrderApprovalEntity.builder()
            .id(orderApproval.getId().getValue())
            .restaurantId(orderApproval.getRestaurantId().getValue())
            .orderId(orderApproval.getOrderId().getValue())
            .approvalStatus(com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalStatus
                .valueOf(orderApproval.getApprovalStatus().name()))
            .build();
    }

    /**
     * OrderApprovalEntityをOrderApprovalドメインに変換
     * @param orderApprovalEntity OrderApprovalEntity
     * @return OrderApprovalドメインオブジェクト
     */
    public OrderApproval orderApprovalEntityToOrderApproval(OrderApprovalEntity orderApprovalEntity) {
        return OrderApproval.builder()
            .orderApprovalId(new OrderApprovalId(orderApprovalEntity.getId()))
            .restaurantId(new RestaurantId(orderApprovalEntity.getRestaurantId()))
            .orderId(new OrderId(orderApprovalEntity.getOrderId()))
            .approvalStatus(com.food.ordering.system.domain.valueobject.OrderApprovalStatus
                .valueOf(orderApprovalEntity.getApprovalStatus().name()))
            .build();
    }
}
