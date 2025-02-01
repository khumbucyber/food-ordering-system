package com.food.ordering.system.order.service.domain;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApplicationDomainEventPublisher
    implements ApplicationEventPublisherAware, DomainEventPublisher<OrderCreatedEvent> {

    private ApplicationEventPublisher applicationDomainEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationDomainEventPublisher = applicationEventPublisher;
    }

    public void publish(OrderCreatedEvent domainEvent) {
        this.applicationDomainEventPublisher.publishEvent(domainEvent);
        log.info("OrderCreatedEvent is published for order id:{}", 
            domainEvent.getOrder().getId().getValue());
    }

}
