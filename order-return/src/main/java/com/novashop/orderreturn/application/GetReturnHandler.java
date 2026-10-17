package com.novashop.orderreturn.application;

import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import java.util.Objects;
import java.util.Optional;

public class GetReturnHandler {

  private final ReturnRequestRepository repository;

  public GetReturnHandler(ReturnRequestRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public Optional<ReturnRequest> handle(String id) {
    Objects.requireNonNull(id, "id");
    return repository.findById(id);
  }
}
