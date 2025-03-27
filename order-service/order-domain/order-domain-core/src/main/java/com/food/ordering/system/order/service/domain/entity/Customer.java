package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.entity.AggregateRoot;

public class Customer extends AggregateRoot<CustomerId> {
    // 空クラスのままとする（16の8:10より）
    // 36の2:16でコンストラクターを実装した。

    // 引数なしのコンストラクター
    public Customer() {
    }

    // 引数ありのコンストラクター
    public Customer(CustomerId customerId) {
        super.setId(customerId);
    }
}
