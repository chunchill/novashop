package com.novashop.orderreturn.application;

import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequestException;
import com.novashop.platform.events.RefundTerminal;
import java.util.Objects;

public final class ApplyRefundTerminalHandler {

  private final ReturnRequestRepository repository;

  public ApplyRefundTerminalHandler(ReturnRequestRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public void handle(RefundTerminal event) {
    Objects.requireNonNull(event, "event");
    ReturnRequest returnRequest =
        repository
            .findById(event.returnRequestId())
            .orElseThrow(
                () ->
                    new ReturnRequestException(
                        "return request not found: " + event.returnRequestId()));
    if (returnRequest.refundId() != null
        && !returnRequest.refundId().equals(event.refundId())) {
      throw new ReturnRequestException(
          "refundId mismatch for return " + event.returnRequestId());
    }
    returnRequest.applyRefundTerminal(event.status());
    repository.save(returnRequest);
  }
}
