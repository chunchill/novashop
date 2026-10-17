package com.novashop.commerce.salesorder.application;

import com.novashop.commerce.salesorder.application.port.OutboxPort;
import com.novashop.commerce.salesorder.application.port.SalesOrderRepository;
import com.novashop.commerce.salesorder.application.port.SkuLookupPort;
import com.novashop.commerce.salesorder.domain.order.OrderLine;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import com.novashop.commerce.salesorder.domain.order.SalesOrderException;
import com.novashop.platform.events.OrderPlaced;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateOrderHandler {

  private final SalesOrderRepository orderRepository;
  private final SkuLookupPort skuLookup;
  private final OutboxPort outbox;

  public CreateOrderHandler(
      SalesOrderRepository orderRepository, SkuLookupPort skuLookup, OutboxPort outbox) {
    this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
    this.skuLookup = Objects.requireNonNull(skuLookup, "skuLookup");
    this.outbox = Objects.requireNonNull(outbox, "outbox");
  }

  public SalesOrder handle(CreateOrderCommand cmd) {
    Objects.requireNonNull(cmd, "cmd");
    if (cmd.orderId() == null || cmd.orderId().isBlank()) {
      throw new SalesOrderException("orderId required");
    }
    if (cmd.lines() == null || cmd.lines().isEmpty()) {
      throw new SalesOrderException("lines required");
    }
    if (orderRepository.findById(cmd.orderId()).isPresent()) {
      throw new SalesOrderException("order already exists: " + cmd.orderId());
    }

    List<OrderLine> lines = new ArrayList<>();
    int i = 1;
    for (CreateOrderCommand.Line line : cmd.lines()) {
      if (line.quantity() <= 0) {
        throw new SalesOrderException("quantity must be positive for sku " + line.skuId());
      }
      SkuLookupPort.SkuQuote quote =
          skuLookup
              .findBySkuId(line.skuId())
              .orElseThrow(() -> new SalesOrderException("unknown sku: " + line.skuId()));
      lines.add(
          new OrderLine(
              "L" + i,
              quote.skuId(),
              quote.name(),
              line.quantity(),
              quote.unitPrice()));
      i++;
    }

    SalesOrder order = SalesOrder.place(cmd.orderId(), lines);
    orderRepository.save(order);
    outbox.append(
        new OrderPlaced(
            order.orderId(),
            order.paymentId(),
            order.total().amount(),
            order.total().currency(),
            order.lines().stream()
                .map(
                    l ->
                        new OrderPlaced.Line(
                            l.skuId(), l.quantity(), l.unitPrice().amount()))
                .toList()));
    return order;
  }
}
