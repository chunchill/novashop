package com.novashop.orderreturn.application;

import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import java.util.Objects;

public class CreateReturnHandler {

  private final ReturnRequestRepository repository;

  public CreateReturnHandler(ReturnRequestRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public String handle(CreateReturnCommand cmd) {
    Objects.requireNonNull(cmd, "cmd");
    if (repository.findById(cmd.id()).isPresent()) {
      throw new IllegalArgumentException("return already exists: " + cmd.id());
    }
    ReturnRequest rr =
        ReturnRequest.pending(
            cmd.id(),
            cmd.paymentId(),
            cmd.refundableTotal(),
            cmd.lines(),
            cmd.alreadyApprovedLineIds());
    repository.save(rr);
    return rr.id();
  }
}
