package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

/*
 * 支払い完了イベント
 * DomainEventPublisherを使用してイベントを発火させる
 * DomainEventPublisherのジェネリック型はPaymentCompletedEventだが、このPaymentCompletedEvent自体が
 * DomainEventPublisherのpublishメソッドに渡されるため、自己参照的な構造となっている
 * この事故参照的な構造は、イベント駆動アーキテクチャにおいて一般的なパターンであり、
 * イベントが発生した際にそのイベント自体を通知するために使用される
 */
public class PaymentCompletedEvent extends PaymentEvent{

    private final DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventPublisher
    ;

    public PaymentCompletedEvent(Payment payment, ZonedDateTime createdAt,
                                DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventPublisher) {
        super(payment, createdAt, Collections.emptyList());
        this.paymentCompletedEventPublisher = paymentCompletedEventPublisher;
    }

    @Override
    public void fire() {
        paymentCompletedEventPublisher.publish(this);
    }

}