package com.novashop.commerce.salesorder.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

  private final BigDecimal amount;
  private final String currency;

  private Money(BigDecimal amount, String currency) {
    this.amount = Objects.requireNonNull(amount, "amount").setScale(2, RoundingMode.UNNECESSARY);
    this.currency = Objects.requireNonNull(currency, "currency");
  }

  public static Money cny(String value) {
    return new Money(new BigDecimal(value), "CNY");
  }

  public static Money of(BigDecimal amount, String currency) {
    return new Money(amount, currency);
  }

  public BigDecimal amount() {
    return amount;
  }

  public String currency() {
    return currency;
  }

  public Money multiply(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity");
    }
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
  }

  public Money add(Money other) {
    Objects.requireNonNull(other, "other");
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException("currency mismatch");
    }
    return new Money(amount.add(other.amount), currency);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money money)) {
      return false;
    }
    return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount.stripTrailingZeros(), currency);
  }

  @Override
  public String toString() {
    return amount + " " + currency;
  }
}
