package com.novashop.commerce.salesorder.domain.order;

import com.novashop.commerce.salesorder.domain.money.Money;
import java.util.Objects;

public final class OrderLine {

  private final String lineId;
  private final String skuId;
  private final String skuName;
  private final int quantity;
  private final Money unitPrice;

  public OrderLine(String lineId, String skuId, String skuName, int quantity, Money unitPrice) {
    this.lineId = Objects.requireNonNull(lineId, "lineId");
    this.skuId = Objects.requireNonNull(skuId, "skuId");
    this.skuName = Objects.requireNonNull(skuName, "skuName");
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity");
    }
    this.quantity = quantity;
    this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
  }

  public Money lineTotal() {
    return unitPrice.multiply(quantity);
  }

  public String lineId() {
    return lineId;
  }

  public String skuId() {
    return skuId;
  }

  public String skuName() {
    return skuName;
  }

  public int quantity() {
    return quantity;
  }

  public Money unitPrice() {
    return unitPrice;
  }
}
