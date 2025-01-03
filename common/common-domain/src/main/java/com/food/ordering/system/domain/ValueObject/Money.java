package com.food.ordering.system.domain.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Money {
    private final BigDecimal amount;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isGreaterThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
        // 小数点を持つBigDicimalでは0とのequalではなく、compareToで比較
    }

    public boolean isGreaterThan(Money money) {
        return this.amount != null && this.amount.compareTo(money.getAmount()) > 0;
    }

    // パラメータで受け取った額を加算して、新しいmoneyオブジェクトを返す
    public Money add(Money money) {
        return new Money(setScale(this.amount.add(money.getAmount())));
    }

    // パラメータで受け取った額を減算して、新しいmoneyオブジェクトを返す
    public Money substract(Money money) {
        return new Money(setScale(this.amount.subtract(money.getAmount())));
    }

    // パラメータで受け取った数を掛け算して、新しいmoneyオブジェクトを返す
    public Money multiply(int multiplier) { // multiplier:掛ける数
        return new Money(setScale(this.amount.multiply(new BigDecimal(multiplier))));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Money other = (Money) obj;
        if (amount == null) {
            if (other.amount != null)
                return false;
        } else if (!amount.equals(other.amount))
            return false;
        return true;
    }
    private BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }
}
