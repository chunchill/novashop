package com.novashop.orderreturn.application;

import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import java.util.List;
import java.util.Objects;

public class ListPendingReturnsHandler {

  private final ReturnRequestRepository repository;

  public ListPendingReturnsHandler(ReturnRequestRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public List<ReturnRequest> handle() {
    return repository.findByStatus(ReturnStatus.PENDING_REVIEW);
  }
}
