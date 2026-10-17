package com.novashop.paymentrefund.interfaces.api;

import com.novashop.paymentrefund.domain.refund.ExternalExecution;
import com.novashop.paymentrefund.domain.refund.Refund;

public record RefundResponse(
    String id,
    String returnRequestId,
    String paymentId,
    String amount,
    String currency,
    String idempotencyKey,
    String status,
    ExternalExecutionResponse latestExecution) {

  public static RefundResponse from(Refund refund) {
    ExternalExecution execution = refund.latestExecution();
    return new RefundResponse(
        refund.id(),
        refund.returnRequestId(),
        refund.paymentId(),
        refund.amount().amount().toPlainString(),
        refund.amount().currency(),
        refund.idempotencyKey(),
        refund.status().name(),
        new ExternalExecutionResponse(
            execution.idempotencyKey(),
            execution.attemptCount(),
            execution.status().name(),
            execution.lastError()));
  }

  public record ExternalExecutionResponse(
      String idempotencyKey, int attemptCount, String status, String lastError) {}
}
