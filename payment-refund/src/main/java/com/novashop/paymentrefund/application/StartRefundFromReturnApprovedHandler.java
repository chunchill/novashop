package com.novashop.paymentrefund.application;

import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.platform.events.ReturnApproved;
import java.util.Objects;

/**
 * Distributed stand-in for Ontology ApproveReturn creating Refund(PENDING_EXTERNAL_EXECUTION)
 * before any PSP call — mirrors the edit-set intent of persisting the refund entity first.
 */
public class StartRefundFromReturnApprovedHandler {

  private final RefundRepository repository;

  public StartRefundFromReturnApprovedHandler(RefundRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public void handle(ReturnApproved event) {
    Objects.requireNonNull(event, "event");

    if (repository.findById(event.refundId()).isPresent()) {
      return;
    }

    Money amount = Money.cny(event.amount().toPlainString());
    Refund refund =
        Refund.createPending(
            event.refundId(),
            event.returnRequestId(),
            event.paymentId(),
            amount,
            event.idempotencyKey());
    repository.save(refund);
  }
}
