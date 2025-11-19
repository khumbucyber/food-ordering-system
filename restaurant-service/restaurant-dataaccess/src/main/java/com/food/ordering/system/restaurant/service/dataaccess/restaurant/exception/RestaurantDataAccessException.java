package com.food.ordering.system.restaurant.service.dataaccess.restaurant.exception;

/**
 * レストランデータアクセス層の例外
 */
public class RestaurantDataAccessException extends RuntimeException {

    public RestaurantDataAccessException(String message) {
        super(message);
    }
}
