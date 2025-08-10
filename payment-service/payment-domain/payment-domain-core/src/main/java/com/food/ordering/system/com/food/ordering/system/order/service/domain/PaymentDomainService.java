package com.food.ordering.system.com.food.ordering.system.order.service.domain;

import java.util.List;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditEntry;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditHistory;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentEvent;

public interface PaymentDomainService {
    
    PaymentEvent validateAndInitiatePayment(Payment payment,
                                            CreditEntry creditEntry,
                                            List<CreditHistory> creditHistories,
                                            List<String> failureMessages);

    PaymentEvent validateAndCancelPayment(Payment payment,
                                            CreditEntry creditEntry,
                                            List<CreditHistory> creditHistories,
                                            List<String> failureMessages);
}
