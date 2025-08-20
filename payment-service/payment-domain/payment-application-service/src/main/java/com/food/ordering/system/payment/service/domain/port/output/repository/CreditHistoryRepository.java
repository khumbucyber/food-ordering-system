package com.food.ordering.system.payment.service.domain.port.output.repository;

import java.util.List;
import java.util.Optional;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditHistory;
import com.food.ordering.system.domain.ValueObject.CustomerId;

public interface CreditHistoryRepository {

    CreditHistory save(CreditHistory creditHistory);

    Optional<List<CreditHistory>> findByCustomerId(CustomerId customerId);
}
