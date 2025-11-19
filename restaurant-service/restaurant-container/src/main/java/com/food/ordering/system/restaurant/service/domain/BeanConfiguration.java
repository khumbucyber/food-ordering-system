package com.food.ordering.system.restaurant.service.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Restaurant ServiceのSpring Bean設定クラス
 */
@Configuration
public class BeanConfiguration {

    /**
     * RestaurantDomainServiceをSpring Beanとして登録
     * @return RestaurantDomainServiceの実装インスタンス
     */
    @Bean
    public RestaurantDomainService restaurantDomainService() {
        return new RestaurantDomainServiceImpl();
    }
}
