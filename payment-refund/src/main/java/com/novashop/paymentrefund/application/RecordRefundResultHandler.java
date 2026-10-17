package com.novashop.paymentrefund.application;

import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.application.port.RefundTerminalOutboxPort;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.domain.refund.RefundStatus;
import com.novashop.platform.events.RefundTerminal;
import java.util.Objects;

public class RecordRefundResultHandler {

  private final RefundRepository repository;
  private final RefundTerminalOutboxPort outbox;

  public RecordRefundResultHandler(RefundRepository repository, RefundTerminalOutboxPort outbox) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.outbox = Objects.requireNonNull(outbox, "outbox");
  }

  public void handle(String refundId, boolean success, String failureReason) {
    Objects.requireNonNull(refundId, "refundId");

    Refund refund =
        repository
            .findById(refundId)
            .orElseThrow(() -> new IllegalArgumentException("refund not found: " + refundId));

    RefundStatus before = refund.status();
    refund.recordResult(success, failureReason);
    repository.save(refund);

    // Skip outbox if already terminal (idempotent no-op on duplicate callback).
    if (before == RefundStatus.SUCCEEDED || before == RefundStatus.FAILED) {
      return;
    }

    outbox.append(
        new RefundTerminal(
            refund.id(),
            refund.returnRequestId(),
            success ? RefundStatus.SUCCEEDED.name() : RefundStatus.FAILED.name(),
            failureReason));
  }
}
