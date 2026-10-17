package com.novashop.commerce.salesorder.domain.order;

import com.novashop.commerce.salesorder.domain.money.Money;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SalesOrder {

  private final String orderId;
  private final String paymentId;
  private final List<OrderLine> lines;
  private final Money total;
  private final Money refundableTotal;

  private SalesOrder(
      String orderId, String paymentId, List<OrderLine> lines, Money total, Money refundableTotal) {
    this.orderId = Objects.requireNonNull(orderId, "orderId");
    this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
    this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    if (this.lines.isEmpty()) {
      throw new IllegalArgumentException("lines");
    }
    this.total = Objects.requireNonNull(total, "total");
    this.refundableTotal = Objects.requireNonNull(refundableTotal, "refundableTotal");
  }

  public static SalesOrder place(String orderId, List<OrderLine> lines) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(lines, "lines");
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("lines");
    }
    Money total = lines.getFirst().lineTotal();
    for (int i = 1; i < lines.size(); i++) {
      total = total.add(lines.get(i).lineTotal());
    }
    // Demo mint only — NOT a Payment bounded context. Real capture/settlement authority is deferred.
    String paymentId = "pay-" + UUID.randomUUID();
    // Phase 2+: full order amount treated as refundable snapshot for teaching returns
    return new SalesOrder(orderId, paymentId, lines, total, total);
  }

  public static SalesOrder reconstitute(
      String orderId, String paymentId, List<OrderLine> lines, Money total, Money refundableTotal) {
    return new SalesOrder(orderId, paymentId, lines, total, refundableTotal);
  }

  public String orderId() {
    return orderId;
  }

  public String paymentId() {
    return paymentId;
  }

  public List<OrderLine> lines() {
    return lines;
  }

  public Money total() {
    return total;
  }

  public Money refundableTotal() {
    return refundableTotal;
  }
}
