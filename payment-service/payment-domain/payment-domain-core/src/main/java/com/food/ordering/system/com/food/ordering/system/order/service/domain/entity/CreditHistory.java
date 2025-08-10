package com.food.ordering.system.com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject.TransactionType;
import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.entity.BaseEntity;

public class CreditHistory extends BaseEntity<CreditHistoryId> {
    private final CustomerId customerId;
    private final Money amount;
    private final TransactionType transactionType;

    private CreditHistory(Builder builder) {
        setId(builder.creditHistoryId);
        this.customerId = builder.customerId;
        this.amount = builder.amount;
        this.transactionType = builder.transactionType;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
    public Money getAmount() {
        return amount;
    }
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CreditHistoryId creditHistoryId;
        private CustomerId customerId;
        private Money amount;
        private TransactionType transactionType;

        public Builder creditHistoryId(CreditHistoryId creditHistoryId) {
            this.creditHistoryId = creditHistoryId;
            return this;
        }
        public Builder customerId(CustomerId customerId) {
            this.customerId = customerId;
            return this;
        }
        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }
        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }
        public CreditHistory build() {
            return new CreditHistory(this);
        }
    }
}
