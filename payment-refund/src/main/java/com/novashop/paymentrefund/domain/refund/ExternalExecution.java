package com.novashop.paymentrefund.domain.refund;

import java.util.Objects;

public final class ExternalExecution {

  private final String idempotencyKey;
  private int attemptCount;
  private ExternalExecutionStatus status;
  private String lastError;

  ExternalExecution(String idempotencyKey) {
    this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey");
    this.attemptCount = 0;
    this.status = ExternalExecutionStatus.PENDING;
    this.lastError = null;
  }

  /** Rehydrate from persistence — does not enforce creation invariants. */
  public static ExternalExecution reconstitute(
      String idempotencyKey,
      int attemptCount,
      ExternalExecutionStatus status,
      String lastError) {
    ExternalExecution execution = new ExternalExecution(idempotencyKey);
    execution.attemptCount = attemptCount;
    execution.status = Objects.requireNonNull(status, "status");
    execution.lastError = lastError;
    return execution;
  }

  void markAttemptStarted() {
    // First attempt starts at 1; retry() already bumps attemptCount for subsequent tries.
    if (attemptCount == 0) {
      this.attemptCount = 1;
    }
    this.status = ExternalExecutionStatus.IN_PROGRESS;
    this.lastError = null;
  }

  void markSucceeded() {
    this.status = ExternalExecutionStatus.SUCCEEDED;
    this.lastError = null;
  }

  void markFailed(String error) {
    this.status = ExternalExecutionStatus.FAILED;
    this.lastError = error;
  }

  void prepareRetry() {
    this.attemptCount += 1;
    this.status = ExternalExecutionStatus.PENDING;
    this.lastError = null;
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }

  public int attemptCount() {
    return attemptCount;
  }

  public ExternalExecutionStatus status() {
    return status;
  }

  public String lastError() {
    return lastError;
  }
}
