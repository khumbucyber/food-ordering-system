package com.food.ordering.system.restaurant.service.dataaccess.restaurant.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.RestaurantEntityId;

/**
 * レストラン情報取得用のJPAリポジトリ
 */
@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

    /**
     * レストランIDと商品IDリストで検索
     * @param restaurantId レストランID
     * @param productIds 商品IDリスト
     * @return レストラン情報と商品情報のリスト
     */
    Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);
}
