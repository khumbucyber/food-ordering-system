package com.food.ordering.system.restaurant.service.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Restaurant ServiceのKafkaトピック設定を保持するクラス
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "restaurant-service")
public class RestaurantServiceConfigData {
    
    /** 注文承認リクエストトピック名 */
    private String restaurantApprovalRequestTopicName;
    
    /** 注文承認レスポンストピック名 */
    private String restaurantApprovalResponseTopicName;
}
