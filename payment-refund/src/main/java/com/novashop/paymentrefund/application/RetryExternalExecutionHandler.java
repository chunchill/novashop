package com.novashop.paymentrefund.application;

import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.domain.refund.Refund;
import java.util.Objects;

public class RetryExternalExecutionHandler {

  private final RefundRepository repository;
  private final ExecuteRefundHandler executeRefundHandler;

  public RetryExternalExecutionHandler(
      RefundRepository repository, ExecuteRefundHandler executeRefundHandler) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.executeRefundHandler =
        Objects.requireNonNull(executeRefundHandler, "executeRefundHandler");
  }

  public void handle(String refundId) {
    Objects.requireNonNull(refundId, "refundId");

    Refund refund =
        repository
            .findById(refundId)
            .orElseThrow(() -> new IllegalArgumentException("refund not found: " + refundId));

    refund.retry();
    repository.save(refund);
    executeRefundHandler.handle(refundId);
  }
}
