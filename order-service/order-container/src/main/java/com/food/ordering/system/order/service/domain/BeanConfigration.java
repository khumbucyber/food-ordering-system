package com.food.ordering.system.order.service.domain;

import org.springframework.context.annotation.Configuration;

// Spting管理のConfigrationBeanとする。
@Configuration
public class BeanConfigration {

    // order-domain-coreのOrderDomainServiceはSpringの依存関係を追加してない。
    // がSpringBeanとして依存性注入したいので、ここでSpring管理にするためのメソッド
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}
