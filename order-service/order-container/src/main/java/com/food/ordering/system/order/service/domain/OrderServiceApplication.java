package com.food.ordering.system.order.service.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// マルチパッケージアプリケーションでの厳密な指定
// このパッケージにあるエンティティのみがJPAエンティティとしてスキャンされる。
@EnableJpaRepositories(basePackages = "com.food.ordering.system.order.service.dataaccess")
@EntityScan(basePackages = "com.food.ordering.system.order.service.dataaccess")
// この指定により、他のモジュールのパッケージは、com.food.ordering.systeで始まる限りスキャンされる。
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
