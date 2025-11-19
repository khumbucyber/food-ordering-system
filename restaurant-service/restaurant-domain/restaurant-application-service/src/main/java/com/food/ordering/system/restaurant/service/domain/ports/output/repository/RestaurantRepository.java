package com.food.ordering.system.restaurant.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;

/**
 * レストラン情報（商品マスタ）取得の出力ポート
 */
public interface RestaurantRepository {

    /**
     * レストラン情報と商品詳細を取得
     * @param restaurant レストランIDと商品IDリストを含むRestaurant
     * @return レストラン情報と商品詳細
     */
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
