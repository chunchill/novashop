package com.novashop.orderreturn.application;

import java.util.Objects;

public record ApproveResult(String returnRequestId, String refundId) {

  public ApproveResult {
    Objects.requireNonNull(returnRequestId, "returnRequestId");
    Objects.requireNonNull(refundId, "refundId");
  }
}
