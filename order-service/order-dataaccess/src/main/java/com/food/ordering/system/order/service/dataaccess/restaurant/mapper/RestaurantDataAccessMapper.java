package com.food.ordering.system.order.service.dataaccess.restaurant.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;

@Component
public class RestaurantDataAccessMapper {

    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        // restaurantからproduct(複数)を取り出して、
        // 1つ1つのproductの中からproductIdをUUIDで取り出し、
        // そのUUIDたちをlist化し、returnする。
        return restaurant.getProducts().stream()
            .map(product -> product.getId().getValue())
                .collect(Collectors.toList());
    }

    // RestaurantEntityは1:1でRestaurantとProductを保持する。
    // RestaurantEntityをListで受け取る。
    // 1つのRestaurant(※1)とNのProductを組み合わせて、
    // Restaurantを作成し、returnする。
    // ※1:StreamクラスのfindFirstメソッドでEntityの1件目をピックアップ
    // Restaurantは1:NでRestaurantとProductを保持する。
    // RestaurantIDが複数混在しないことが前提（チェックなし）となっている点は注意
    public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity restaurantEntity = 
            restaurantEntities.stream().findFirst().orElseThrow(() ->
                new RestaurantDataAccessException("Restaurant could not be found!"));

        List<Product> products =
            restaurantEntities.stream().map(entity -> new Product(
                new ProductId(entity.getProductId()),
                entity.getProductName(), new Money(entity.getProductPrice())))
                .collect(Collectors.toList());

        Restaurant restaurant = Restaurant.builder()
            .setRestaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
            .setActive(restaurantEntity.getRestaurantActive())
            .setProducts(products)
            .build();

        return restaurant;
    }
}
