package com.food.ordering.system.payment.service.domain.port.output.message.publisher;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;

public interface PaymentCompletedMessagePublisher 
    extends DomainEventPublisher<PaymentCompletedEvent>{



}
