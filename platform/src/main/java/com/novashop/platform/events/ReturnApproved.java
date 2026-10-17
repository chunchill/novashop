package com.novashop.platform.events;

import java.math.BigDecimal;
import java.util.List;

public record ReturnApproved(
    String refundId,
    String returnRequestId,
    String paymentId,
    BigDecimal amount,
    String currency,
    List<String> approvedLineIds,
    String actorId,
    String actorRole,
    String reason,
    String resolutionCode,
    String idempotencyKey) {

  public ReturnApproved {
    requireNonNull(refundId, "refundId");
    requireNonNull(returnRequestId, "returnRequestId");
    requireNonNull(paymentId, "paymentId");
    requireNonNull(amount, "amount");
    requireNonNull(currency, "currency");
    requireNonNull(approvedLineIds, "approvedLineIds");
    approvedLineIds = List.copyOf(approvedLineIds);
    requireNonNull(actorId, "actorId");
    requireNonNull(actorRole, "actorRole");
    requireNonNull(reason, "reason");
    requireNonNull(resolutionCode, "resolutionCode");
    requireNonNull(idempotencyKey, "idempotencyKey");
  }

  private static void requireNonNull(Object value, String name) {
    if (value == null) {
      throw new IllegalArgumentException(name);
    }
  }
}
