package com.food.ordering.system.restaurant.service.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Order Serviceからの注文承認リクエストを表すDTO
 */
@Getter
@Builder
@AllArgsConstructor
public class RestaurantApprovalRequest {

    /** イベントID */
    private String id;
    /** Sagaトランザクション管理ID */
    private String sagaId;
    /** レストランID */
    private String restaurantId;
    /** 注文ID */
    private String orderId;
    /** 注文ステータス */
    private RestaurantOrderStatus restaurantOrderStatus;
    /** 注文商品リスト */
    private List<Product> products;
    /** 合計金額 */
    private BigDecimal price;
    /** 作成日時 */
    private Instant createdAt;
}
