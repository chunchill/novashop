package com.novashop.commerce.salesorder.interfaces.api;

import com.novashop.commerce.salesorder.application.CreateOrderCommand;
import com.novashop.commerce.salesorder.infrastructure.config.SalesOrderHandlersConfig.CreateOrderService;
import com.novashop.commerce.salesorder.infrastructure.config.SalesOrderHandlersConfig.GetOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final CreateOrderService createOrderService;
  private final GetOrderService getOrderService;

  public OrderController(CreateOrderService createOrderService, GetOrderService getOrderService) {
    this.createOrderService = createOrderService;
    this.getOrderService = getOrderService;
  }

  @PostMapping
  public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest body) {
    String orderId =
        body.orderId() == null || body.orderId().isBlank()
            ? "ord-" + UUID.randomUUID()
            : body.orderId();
    var order =
        createOrderService.handle(
            new CreateOrderCommand(
                orderId,
                body.lines().stream()
                    .map(l -> new CreateOrderCommand.Line(l.skuId(), l.quantity()))
                    .toList()));
    return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> get(@PathVariable String orderId) {
    return getOrderService
        .handle(orderId)
        .map(OrderResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
