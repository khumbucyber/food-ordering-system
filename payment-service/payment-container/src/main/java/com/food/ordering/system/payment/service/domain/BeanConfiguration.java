package com.food.ordering.system.payment.service.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring管理のConfigurationBeanとする。
@Configuration
public class BeanConfiguration {

    // payment-domain-coreのPaymentDomainServiceはSpringの依存関係を追加してない。
    // がSpringBeanとして依存性注入したいので、ここでSpring管理にするためのメソッド
    @Bean
    public PaymentDomainService paymentDomainService() {
        return new PaymentDomainServiceImpl();
    }
}
