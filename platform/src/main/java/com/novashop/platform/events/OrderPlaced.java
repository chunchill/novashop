package com.novashop.platform.events;

import java.math.BigDecimal;
import java.util.List;

/** Thin integration contract: sales order placed (Phase 2 teaching outbox). */
public record OrderPlaced(
    String orderId,
    String paymentId,
    BigDecimal totalAmount,
    String currency,
    List<Line> lines) {

  public OrderPlaced {
    requireNonNull(orderId, "orderId");
    requireNonNull(paymentId, "paymentId");
    requireNonNull(totalAmount, "totalAmount");
    requireNonNull(currency, "currency");
    requireNonNull(lines, "lines");
    lines = List.copyOf(lines);
  }

  public record Line(String skuId, int quantity, BigDecimal unitPrice) {
    public Line {
      requireNonNull(skuId, "skuId");
      if (quantity <= 0) {
        throw new IllegalArgumentException("quantity");
      }
      requireNonNull(unitPrice, "unitPrice");
    }
  }

  private static void requireNonNull(Object value, String name) {
    if (value == null) {
      throw new IllegalArgumentException(name);
    }
  }
}
