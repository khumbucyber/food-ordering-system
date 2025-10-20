package com.food.ordering.system.order.service.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spting管理のConfigrationBeanとする。
@Configuration
public class BeanConfiguration {

    // order-domain-coreのOrderDomainServiceはSpringの依存関係を追加してない。
    // がSpringBeanとして依存性注入したいので、ここでSpring管理にするためのメソッド
    @Bean   // Beanアノテーションを付けることで、SpringがこのメソッドをBeanとして管理する。←抜けてたので追加
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}
