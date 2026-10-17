package com.novashop.commerce.salesorder.application;

import com.novashop.commerce.salesorder.application.port.SalesOrderRepository;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.Objects;
import java.util.Optional;

public class GetOrderHandler {

  private final SalesOrderRepository repository;

  public GetOrderHandler(SalesOrderRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public Optional<SalesOrder> handle(String orderId) {
    Objects.requireNonNull(orderId, "orderId");
    return repository.findById(orderId);
  }
}
