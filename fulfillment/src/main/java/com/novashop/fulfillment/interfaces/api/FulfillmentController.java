package com.novashop.fulfillment.interfaces.api;

import com.novashop.fulfillment.infrastructure.config.ApplicationHandlersConfig.GetFulfillmentByOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fulfillments")
public class FulfillmentController {

  private final GetFulfillmentByOrderService getFulfillmentByOrderService;

  public FulfillmentController(GetFulfillmentByOrderService getFulfillmentByOrderService) {
    this.getFulfillmentByOrderService = getFulfillmentByOrderService;
  }

  @GetMapping("/by-order/{orderId}")
  public ResponseEntity<FulfillmentResponse> byOrder(@PathVariable String orderId) {
    return getFulfillmentByOrderService
        .handle(orderId)
        .map(FulfillmentResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
