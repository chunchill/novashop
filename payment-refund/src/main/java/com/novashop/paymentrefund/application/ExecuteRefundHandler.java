package com.novashop.paymentrefund.application;

import com.novashop.paymentrefund.application.port.PspRefundClient;
import com.novashop.paymentrefund.application.port.PspRefundResult;
import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.application.port.RefundTerminalOutboxPort;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.domain.refund.RefundStatus;
import com.novashop.platform.events.RefundTerminal;
import java.util.Objects;

public class ExecuteRefundHandler {

  private final RefundRepository repository;
  private final PspRefundClient pspClient;
  private final RefundTerminalOutboxPort outbox;

  public ExecuteRefundHandler(
      RefundRepository repository, PspRefundClient pspClient, RefundTerminalOutboxPort outbox) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.pspClient = Objects.requireNonNull(pspClient, "pspClient");
    this.outbox = Objects.requireNonNull(outbox, "outbox");
  }

  public void handle(String refundId) {
    Objects.requireNonNull(refundId, "refundId");

    Refund refund =
        repository
            .findById(refundId)
            .orElseThrow(() -> new IllegalArgumentException("refund not found: " + refundId));

    refund.recordExecutionAttempt();
    repository.save(refund);

    // Idempotency key = refund id (minted upstream on approve).
    PspRefundResult result =
        pspClient.refund(
            refund.id(), refund.idempotencyKey(), refund.amount(), refund.paymentId());

    switch (result.outcome()) {
      case PENDING_CALLBACK -> {
        // Leave IN_PROGRESS for async PSP callback → RecordRefundResultHandler.
      }
      case SUCCEEDED -> applyTerminal(refund, true, null);
      case FAILED -> applyTerminal(refund, false, result.failureReason());
    }
  }

  private void applyTerminal(Refund refund, boolean success, String failureReason) {
    refund.recordResult(success, failureReason);
    repository.save(refund);
    outbox.append(
        new RefundTerminal(
            refund.id(),
            refund.returnRequestId(),
            success ? RefundStatus.SUCCEEDED.name() : RefundStatus.FAILED.name(),
            failureReason));
  }
}
