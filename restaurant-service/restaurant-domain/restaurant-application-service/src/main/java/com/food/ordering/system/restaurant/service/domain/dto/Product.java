package com.food.ordering.system.restaurant.service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 注文商品を表すDTO
 */
@Getter
@Builder
@AllArgsConstructor
public class Product {

    /** 商品ID */
    private String id;
    /** 数量 */
    private Integer quantity;
}
