package com.novashop.paymentrefund.domain.refund;

import com.novashop.paymentrefund.domain.money.Money;
import java.util.Objects;

public final class Refund {

  private final String id;
  private final String returnRequestId;
  private final String paymentId;
  private final Money amount;
  private final String idempotencyKey;
  private RefundStatus status;
  private final ExternalExecution latestExecution;

  private Refund(
      String id,
      String returnRequestId,
      String paymentId,
      Money amount,
      String idempotencyKey,
      RefundStatus status,
      ExternalExecution latestExecution) {
    this.id = Objects.requireNonNull(id, "id");
    this.returnRequestId = Objects.requireNonNull(returnRequestId, "returnRequestId");
    this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
    this.amount = Objects.requireNonNull(amount, "amount");
    this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey");
    this.status = Objects.requireNonNull(status, "status");
    this.latestExecution = Objects.requireNonNull(latestExecution, "latestExecution");
  }

  /**
   * Creates a Refund in {@link RefundStatus#PENDING_EXTERNAL_EXECUTION} with an initial
   * {@link ExternalExecution} intent — before any PSP call succeeds.
   */
  public static Refund createPending(
      String refundId,
      String returnRequestId,
      String paymentId,
      Money amount,
      String idempotencyKey) {
    return new Refund(
        refundId,
        returnRequestId,
        paymentId,
        amount,
        idempotencyKey,
        RefundStatus.PENDING_EXTERNAL_EXECUTION,
        new ExternalExecution(idempotencyKey));
  }

  /** Rehydrate from persistence — does not enforce pending-only invariants. */
  public static Refund reconstitute(
      String id,
      String returnRequestId,
      String paymentId,
      Money amount,
      String idempotencyKey,
      RefundStatus status,
      ExternalExecution latestExecution) {
    return new Refund(
        id, returnRequestId, paymentId, amount, idempotencyKey, status, latestExecution);
  }

  public void recordExecutionAttempt() {
    if (status != RefundStatus.PENDING_EXTERNAL_EXECUTION) {
      throw new RefundException("execution attempt requires PENDING_EXTERNAL_EXECUTION");
    }
    if (latestExecution.status() != ExternalExecutionStatus.PENDING) {
      throw new RefundException("execution attempt requires ExternalExecution PENDING");
    }
    latestExecution.markAttemptStarted();
  }

  /**
   * Records PSP outcome. Duplicate calls with the same idempotency key are a no-op once terminal.
   * Success is reachable only through this path — never via createPending / recordExecutionAttempt.
   */
  public void recordResult(boolean success, String error) {
    if (status == RefundStatus.SUCCEEDED || status == RefundStatus.FAILED) {
      if (idempotencyKey.equals(latestExecution.idempotencyKey())) {
        return;
      }
      throw new RefundException("refund already terminal with different outcome context");
    }
    if (status != RefundStatus.PENDING_EXTERNAL_EXECUTION) {
      throw new RefundException("recordResult requires PENDING_EXTERNAL_EXECUTION");
    }

    if (success) {
      latestExecution.markSucceeded();
      this.status = RefundStatus.SUCCEEDED;
    } else {
      latestExecution.markFailed(error);
      this.status = RefundStatus.FAILED;
    }
  }

  public void retry() {
    if (status != RefundStatus.FAILED) {
      throw new RefundException("retry requires FAILED status");
    }
    latestExecution.prepareRetry();
    this.status = RefundStatus.PENDING_EXTERNAL_EXECUTION;
  }

  public String id() {
    return id;
  }

  public String returnRequestId() {
    return returnRequestId;
  }

  public String paymentId() {
    return paymentId;
  }

  public Money amount() {
    return amount;
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }

  public RefundStatus status() {
    return status;
  }

  public ExternalExecution latestExecution() {
    return latestExecution;
  }
}
