package com.food.ordering.system.order.service.domain.dto.create;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.valueobject.OrderAddress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// domain-coreではBuilderを手動で作成したが、ここではアノテーションを使用する。
@Getter
@Builder
@AllArgsConstructor
public class CreateOrderComman {
    @NotNull
    private final UUID customerId;
    @NotNull
    private final UUID restaurantId;
    @NotNull
    private final BigDecimal price;
    @NotNull
    private final List<OrderItem> items;
    @NotNull
    private final OrderAddress address;
}
