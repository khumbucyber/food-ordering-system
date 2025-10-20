package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.PaymentDomainService;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentRequestHelper {

    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;
    private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;

    public PaymentRequestHelper(PaymentDomainService paymentDomainService, 
                                PaymentDataMapper paymentDataMapper,
                                PaymentRepository paymentRepository, 
                                CreditEntryRepository creditEntryRepository,
                                CreditHistoryRepository creditHistoryRepository,
                                PaymentCompletedMessagePublisher paymentCompletedMessagePublisher,
                                PaymentCancelledMessagePublisher paymentCancelledMessagePublisher,
                                PaymentFailedMessagePublisher paymentFailedMessagePublisher) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.paymentCompletedMessagePublisher = paymentCompletedMessagePublisher;
        this.paymentCancelledMessagePublisher = paymentCancelledMessagePublisher;
        // メモ このPublisherを各メソッドに渡すようにする⇒完了
        this.paymentFailedMessagePublisher = paymentFailedMessagePublisher;
    }

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Persisting payment for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
        return persistAndSave(
            payment,
            (p, ce, ch, fm, publisher, failedPublisher) -> paymentDomainService.validateAndInitiatePayment(p, ce, ch, fm, publisher, failedPublisher),
            paymentCompletedMessagePublisher
        );
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        log.info("Persisting cancel payment for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> paymentResponse = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Could not find payment for order id: {}", paymentRequest.getOrderId());
            throw new PaymentApplicationServiceException("Could not find payment for order id: " + paymentRequest.getOrderId());
        }
        Payment payment = paymentResponse.get();
        return persistAndSave(
            payment,
            (p, ce, ch, fm, publisher, failedPublisher) -> paymentDomainService.validateAndCancelPayment(p, ce, ch, fm, publisher, failedPublisher),
            paymentCancelledMessagePublisher
        );
    }
    // 共通処理をまとめたprivateメソッド
    // validateAndInitiatePaymentやvalidateAndCancelPaymentの処理を共通化
    private <T extends PaymentMessagePublisher> PaymentEvent persistAndSave(
            Payment payment,
            PaymentEventProcessor<T> paymentEventProcessor,
            T messagePublisher
    ) {
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = paymentEventProcessor.process(payment, creditEntry, creditHistories, failureMessages, messagePublisher, paymentFailedMessagePublisher);
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            // 最後の履歴レコードをsaveする
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
        }
        return paymentEvent;
    }

    @FunctionalInterface
    private interface PaymentEventProcessor<T extends PaymentMessagePublisher> {
        PaymentEvent process(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages, T messagePublisher, PaymentFailedMessagePublisher failedMessagePublisher);
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
        if (creditEntry.isEmpty()) {
            log.error("Could not find credit entry for customer id: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit entry for customer id: " + customerId.getValue());
        }
        return creditEntry.get();
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        Optional<List<CreditHistory>> creditHistories = creditHistoryRepository.findByCustomerId(customerId);
        if (creditHistories.isEmpty()) {
            log.error("Could not find credit history for customer id: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit history for customer id: " + customerId.getValue());
        }
        return creditHistories.get();
    }

}
