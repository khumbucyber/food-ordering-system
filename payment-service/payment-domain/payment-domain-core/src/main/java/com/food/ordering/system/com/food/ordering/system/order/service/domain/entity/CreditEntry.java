package com.food.ordering.system.com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.com.food.ordering.system.order.service.domain.valueobject.CreditEntryId;
import com.food.ordering.system.domain.ValueObject.CustomerId;
import com.food.ordering.system.domain.ValueObject.Money;
import com.food.ordering.system.domain.entity.BaseEntity;

public class CreditEntry extends BaseEntity<CreditEntryId>{

    private final CustomerId customerId;
    private Money totalCreditAmount;

    private CreditEntry(Builder builder) {
        setId(builder.creditEntryId);
        this.customerId = builder.customerId;
        this.totalCreditAmount = builder.totalCreditAmount;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public void addCreditAmount(Money amount) {
        this.totalCreditAmount = totalCreditAmount.add(amount);
    }

    public void subtractCreditAmount(Money amount) {
        this.totalCreditAmount = totalCreditAmount.subtract(amount);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CreditEntryId creditEntryId;
        private CustomerId customerId;
        private Money totalCreditAmount;

        public Builder creditEntryId(CreditEntryId creditEntryId) {
            this.creditEntryId = creditEntryId;
            return this;
        }
        public Builder customerId(CustomerId customerId) {
            this.customerId = customerId;
            return this;
        }
        public Builder totalCreditAmount(Money totalCreditAmount) {
            this.totalCreditAmount = totalCreditAmount;
            return this;
        }
        public CreditEntry build() {
            return new CreditEntry(this);
        }
    }
}
