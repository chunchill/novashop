package com.novashop.platform.events;

public record RefundTerminal(
    String refundId, String returnRequestId, String status, String failureReason) {

  public RefundTerminal {
    requireNonNull(refundId, "refundId");
    requireNonNull(returnRequestId, "returnRequestId");
    requireNonNull(status, "status");
  }

  private static void requireNonNull(Object value, String name) {
    if (value == null) {
      throw new IllegalArgumentException(name);
    }
  }
}
