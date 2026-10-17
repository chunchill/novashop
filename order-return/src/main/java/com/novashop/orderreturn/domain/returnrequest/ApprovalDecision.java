package com.novashop.orderreturn.domain.returnrequest;

import java.util.Objects;

public record ApprovalDecision(String refundId, ReturnStatus status) {

  public ApprovalDecision {
    Objects.requireNonNull(refundId, "refundId");
    if (refundId.isBlank()) {
      throw new IllegalArgumentException("refundId");
    }
    Objects.requireNonNull(status, "status");
  }
}
