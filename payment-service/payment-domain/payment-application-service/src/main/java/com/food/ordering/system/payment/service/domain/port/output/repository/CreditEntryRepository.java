package com.food.ordering.system.payment.service.domain.port.output.repository;

import java.util.Optional;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditEntry;
import com.food.ordering.system.domain.ValueObject.CustomerId;

public interface CreditEntryRepository {

    CreditEntry save(CreditEntry creditEntry);

    Optional<CreditEntry> findByCustomerId(CustomerId customerId);

}
