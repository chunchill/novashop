package com.novashop.commerce.unit.salesorder.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.commerce.salesorder.application.CreateOrderCommand;
import com.novashop.commerce.salesorder.application.CreateOrderHandler;
import com.novashop.commerce.salesorder.application.port.OutboxPort;
import com.novashop.commerce.salesorder.application.port.SalesOrderRepository;
import com.novashop.commerce.salesorder.application.port.SkuLookupPort;
import com.novashop.commerce.salesorder.domain.money.Money;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import com.novashop.commerce.salesorder.domain.order.SalesOrderException;
import com.novashop.platform.events.OrderPlaced;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CreateOrderHandlerTest {

  @Test
  void createsOrder_persistsAndAppendsOrderPlaced() {
    var repo = new InMemoryOrderRepo();
    var outbox = new CapturingOutbox();
    var handler =
        new CreateOrderHandler(
            repo,
            skuId ->
                Optional.of(new SkuLookupPort.SkuQuote(skuId, "Mug", Money.cny("49.00"))),
            outbox);

    SalesOrder order =
        handler.handle(
            new CreateOrderCommand(
                "ord-demo", List.of(new CreateOrderCommand.Line("sku-mug-std", 2))));

    assertThat(order.total().amount()).isEqualByComparingTo("98.00");
    assertThat(repo.findById("ord-demo")).isPresent();
    assertThat(outbox.events).hasSize(1);
    assertThat(outbox.events.getFirst()).isInstanceOf(OrderPlaced.class);
    OrderPlaced event = (OrderPlaced) outbox.events.getFirst();
    assertThat(event.orderId()).isEqualTo("ord-demo");
    assertThat(event.paymentId()).isEqualTo(order.paymentId());
  }

  @Test
  void rejectsUnknownSku() {
    var handler =
        new CreateOrderHandler(
            new InMemoryOrderRepo(), skuId -> Optional.empty(), new CapturingOutbox());

    assertThatThrownBy(
            () ->
                handler.handle(
                    new CreateOrderCommand(
                        "ord-x", List.of(new CreateOrderCommand.Line("missing", 1)))))
        .isInstanceOf(SalesOrderException.class)
        .hasMessageContaining("unknown sku");
  }

  static final class InMemoryOrderRepo implements SalesOrderRepository {
    private final Map<String, SalesOrder> store = new HashMap<>();

    @Override
    public void save(SalesOrder order) {
      store.put(order.orderId(), order);
    }

    @Override
    public Optional<SalesOrder> findById(String orderId) {
      return Optional.ofNullable(store.get(orderId));
    }
  }

  static final class CapturingOutbox implements OutboxPort {
    final List<Object> events = new ArrayList<>();

    @Override
    public void append(Object event) {
      events.add(event);
    }
  }
}
