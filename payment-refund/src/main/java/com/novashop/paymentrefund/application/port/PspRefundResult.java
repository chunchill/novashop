package com.novashop.paymentrefund.application.port;

import java.util.Objects;

public record PspRefundResult(Outcome outcome, String failureReason) {

  public enum Outcome {
    SUCCEEDED,
    FAILED,
    PENDING_CALLBACK
  }

  public PspRefundResult {
    Objects.requireNonNull(outcome, "outcome");
  }

  public static PspRefundResult succeeded() {
    return new PspRefundResult(Outcome.SUCCEEDED, null);
  }

  public static PspRefundResult failed(String failureReason) {
    return new PspRefundResult(Outcome.FAILED, failureReason);
  }

  public static PspRefundResult pendingCallback() {
    return new PspRefundResult(Outcome.PENDING_CALLBACK, null);
  }
}
