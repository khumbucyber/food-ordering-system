package com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject;

import java.util.UUID;

import com.food.ordering.system.domain.ValueObject.BaseId;

public class CreditEntryId extends BaseId<UUID> {

    public CreditEntryId(UUID value) {
        super(value);
    }
}
