package com.food.ordering.system.restaurant.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Restaurant Serviceのメインアプリケーションクラス
 */
@EnableJpaRepositories(basePackages = "com.food.ordering.system.restaurant.service.dataaccess")
@EntityScan(basePackages = "com.food.ordering.system.restaurant.service.dataaccess")
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class RestaurantServiceApplication {
    
    /**
     * アプリケーションのエントリーポイント
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
