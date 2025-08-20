package com.food.ordering.system.com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditEntry;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.CreditHistory;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentEvent;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject.TransactionType;
import com.food.ordering.system.domain.DomainConstants;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.ValueObject.PaymentStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService{

    @Override
    public PaymentEvent validateAndInitiatePayment(Payment payment,
                                                   CreditEntry creditEntry,
                                                   List<CreditHistory> creditHistories,
                                                   List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHisory(payment, creditHistories, TransactionType.DEBIT);
        validateCreditHitory(creditEntry, creditHistories, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info("Payment for order: {} is successfully initiated with price: {}",
                    payment.getOrderId().getValue(), payment.getPrice().getAmount());
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
        } else {
            log.error("Payment for order: {} failed", 
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(), failureMessages);
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(Payment payment,
                                                 CreditEntry creditEntry,
                                                 List<CreditHistory> creditHistories,
                                                 List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        addCreditAmount(payment, creditEntry);
        updateCreditHisory(payment, creditHistories, TransactionType.CREDIT);

        if (failureMessages.isEmpty()) {
            log.info("Payment for order: {} is successfully cancelled with price: {}",
                    payment.getOrderId().getValue(), payment.getPrice().getAmount());
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(DomainConstants.UTC)));
        } else {
            log.error("Payment for order: {} cancellation failed", 
                    payment.getOrderId().getValue());
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(), failureMessages);
        }
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Cutomer with id: {} doesnt't have enough credit for payment for order: {}",
                    payment.getCustomerId().getValue(), payment.getOrderId().getValue());
            failureMessages.add("Customer with id: " + payment.getCustomerId().getValue() +
                    " doesn't have enough credit for payment for order: " + payment.getOrderId().getValue());
        }
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void updateCreditHisory(Payment payment, List<CreditHistory> creditHistories, TransactionType transactionType) {
        creditHistories.add(CreditHistory.builder()
                .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                .customerId(payment.getCustomerId())
                .amount(payment.getPrice())
                .transactionType(transactionType)
                .build());
    }

    private void validateCreditHitory(CreditEntry creditEntry, List<CreditHistory> creditHistories,
            List<String> failureMessages) {
        Money totalCreditHistoryAmount = getTotalHistoryAmount(creditHistories, TransactionType.CREDIT);
        Money totalDebitHistoryAmount = getTotalHistoryAmount(creditHistories, TransactionType.DEBIT);
        
        if (totalDebitHistoryAmount.isGreaterThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} has total debit amount: {} greater than total credit amount: {}",
                    creditEntry.getCustomerId().getValue(), totalDebitHistoryAmount.getAmount(), creditEntry.getTotalCreditAmount().getAmount());
            failureMessages.add("Customer with id: " + creditEntry.getCustomerId().getValue() +
                    " has total debit amount: " + totalDebitHistoryAmount.getAmount() +
                    " greater than total credit amount: " + creditEntry.getTotalCreditAmount().getAmount());
        }

        if (!creditEntry.getTotalCreditAmount().equals(totalCreditHistoryAmount.subtract(totalDebitHistoryAmount))) {
            log.error("Customer with id: {} has total credit amount: {} not equal to total credit history amount: {}",
                    creditEntry.getCustomerId().getValue(), creditEntry.getTotalCreditAmount().getAmount(),
                    totalCreditHistoryAmount.subtract(totalDebitHistoryAmount).getAmount());
            failureMessages.add("Customer with id: " + creditEntry.getCustomerId().getValue() +
                    " has total credit amount: " + creditEntry.getTotalCreditAmount().getAmount() +
                    " not equal to total credit history amount: " + totalCreditHistoryAmount.getAmount());
        }


    }

    private Money getTotalHistoryAmount(List<CreditHistory> creditHistories, TransactionType transactionType) {
        return creditHistories.stream()
                .filter(creditHistory -> transactionType == creditHistory.getTransactionType())
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    private void addCreditAmount(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }

}
