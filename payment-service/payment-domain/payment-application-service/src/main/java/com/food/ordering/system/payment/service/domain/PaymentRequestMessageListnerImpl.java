package com.food.ordering.system.payment.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.ports.input.message.listner.PaymentRequestMessageListner;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;

import lombok.extern.slf4j.Slf4j;

/* 
 * 支払いリクエストのメッセージをリッスンし、メッセージを検知した際に支払い処理を行うクラス
 * 支払いの完了、キャンセル、失敗の各イベントに対応するメッセージパブリッシャーを使用して、対応するイベントを発行
 */
@Slf4j
@Service
public class PaymentRequestMessageListnerImpl implements PaymentRequestMessageListner{

    public final PaymentRequestHelper paymentRequestHelper;
    public final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
    public final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;
    public final PaymentFailedMessagePublisher paymentFailedMessagePublisher;

    // コンストラクタインジェクション
    public PaymentRequestMessageListnerImpl(PaymentRequestHelper paymentRequestHelper,
            PaymentCompletedMessagePublisher paymentCompletedMessagePublisher,
            PaymentCancelledMessagePublisher paymentCancelledMessagePublisher,
            PaymentFailedMessagePublisher paymentFailedMessagePublisher) {
        this.paymentRequestHelper = paymentRequestHelper;
        this.paymentCompletedMessagePublisher = paymentCompletedMessagePublisher;
        this.paymentCancelledMessagePublisher = paymentCancelledMessagePublisher;
        this.paymentFailedMessagePublisher = paymentFailedMessagePublisher;
    }
    
    @Override
    public void completePayment(PaymentRequest paymentRequest) {
        // 支払いの永続化と検証を行い、結果としてPaymentEventを取得
        PaymentEvent paymentEvent = paymentRequestHelper.persistPayment(paymentRequest);
        fireEvent(paymentEvent);
    }

    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistCancelPayment(paymentRequest);
        fireEvent(paymentEvent);
    }

    // eventの発行を行うprivateメソッド
    private void fireEvent(PaymentEvent paymentEvent) {
        log.info("Publishing payment event with paymentId: {} and orderId: {]}",
            paymentEvent.getPayment().getId().getValue(), 
            paymentEvent.getPayment().getOrderId().getValue());
        
        // Eventの型をチェックし、対応するメッセージパブリッシャーを使用してイベントを発行する。
        // しかし、if-elseの連続はあまり良い設計とは言えない。
        // そのため、Eventの型をチェックせずに、Event発行を行うようにリファクタリングする。
        // 例えば、Eventクラスにfireメソッドを追加し、各Eventクラスでその実装を行う。
        // 但しそうすると、Eventクラスがメッセージパブリッシャーに依存してしまう。
        // これは、domain-coreモジュールがapplication-serviceモジュールに依存してしまうことを意味し、望ましくない。
        // そのため、eventクラスからcommon-domainにあるDomainEventPublisher.javaを継承したインターフェースを作成し、Eventクラスにそのインターフェースを実装させる。
        // しかし、そもそもDomainEventPublisher.javaがcommon-domainに存在するべきなのか？
        // if (paymentEvent instanceof PaymentCompletedEvent) {
        //     log.info("Publishing payment completed event");
        //     paymentCompletedMessagePublisher.publish((PaymentCompletedEvent) paymentEvent);
        // } else if (paymentEvent instanceof PaymentCancelledEvent) {
        //     log.info("Publishing payment cancelled event");
        //     paymentCancelledMessagePublisher.publish((PaymentCancelledEvent) paymentEvent);
        // } else if (paymentEvent instanceof PaymentFailedEvent) {
        //     log.info("Publishing payment failed event");
        //     paymentFailedMessagePublisher.publish((PaymentFailedEvent) paymentEvent);
        // }
        paymentEvent.fire();
    }
}
