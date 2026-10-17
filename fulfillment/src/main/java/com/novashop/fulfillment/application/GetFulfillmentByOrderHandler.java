package com.novashop.fulfillment.application;

import com.novashop.fulfillment.application.port.FulfillmentOrderRepository;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;
import java.util.Objects;
import java.util.Optional;

public class GetFulfillmentByOrderHandler {

  private final FulfillmentOrderRepository fulfillmentOrderRepository;

  public GetFulfillmentByOrderHandler(FulfillmentOrderRepository fulfillmentOrderRepository) {
    this.fulfillmentOrderRepository =
        Objects.requireNonNull(fulfillmentOrderRepository, "fulfillmentOrderRepository");
  }

  public Optional<FulfillmentOrder> handle(String orderId) {
    return fulfillmentOrderRepository.findByOrderId(orderId);
  }
}
