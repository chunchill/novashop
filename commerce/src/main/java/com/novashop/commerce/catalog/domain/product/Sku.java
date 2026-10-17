package com.novashop.commerce.catalog.domain.product;

import com.novashop.commerce.catalog.domain.money.Money;
import java.util.Objects;

public final class Sku {

  private final String skuId;
  private final String name;
  private final Money unitPrice;

  public Sku(String skuId, String name, Money unitPrice) {
    this.skuId = Objects.requireNonNull(skuId, "skuId");
    this.name = Objects.requireNonNull(name, "name");
    this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
  }

  public String skuId() {
    return skuId;
  }

  public String name() {
    return name;
  }

  public Money unitPrice() {
    return unitPrice;
  }
}
