package com.food.ordering.system.payment.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.PaymentDomainService;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditEntry;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditHistory;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentEvent;
import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.port.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.port.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.port.output.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentRequestHelper {

    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    public PaymentRequestHelper(PaymentDomainService paymentDomainService, PaymentDataMapper paymentDataMapper,
            PaymentRepository paymentRepository, CreditEntryRepository creditEntryRepository,
            CreditHistoryRepository creditHistoryRepository) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
    }

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Persisting payment for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHisotries = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = 
            paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHisotries, failureMessages);
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            // 最後の履歴レコードをsaveする
            creditHistoryRepository.save(creditHisotries.get(creditHisotries.size() - 1));
        }
        return paymentEvent;
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        Optional<Payment> payment = paymentRepository.findByOrderId(paymentRequest.getOrderId());
        if (payment.isEmpty()) {
            log.error("Could not find payment for order id: {}", paymentRequest.getOrderId());
            throw new PaymentApplicationServiceException("Could not find payment for order id: " + paymentRequest.getOrderId());
        }
        return null;
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
