package com.novashop.fulfillment.unit.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.fulfillment.application.ReserveStockFromOrderPlacedHandler;
import com.novashop.fulfillment.application.port.FulfillmentOrderRepository;
import com.novashop.fulfillment.application.port.InventoryRepository;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentStatus;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import com.novashop.platform.events.OrderPlaced;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReserveStockFromOrderPlacedHandlerTest {

  @Test
  void allocates_whenStockSufficient() {
    var inventory = new InMemoryInventory();
    inventory.save(InventoryPosition.seed("sku-mug-std", 10));
    var fulfillments = new InMemoryFulfillments();
    var handler = new ReserveStockFromOrderPlacedHandler(inventory, fulfillments, "WH-1");

    FulfillmentOrder result =
        handler.handle(
            new OrderPlaced(
                "ord-1",
                "pay-1",
                new BigDecimal("49.00"),
                "CNY",
                List.of(new OrderPlaced.Line("sku-mug-std", 2, new BigDecimal("49.00")))));

    assertThat(result.status()).isEqualTo(FulfillmentStatus.ALLOCATED);
    assertThat(result.warehouseCode()).isEqualTo("WH-1");
    assertThat(inventory.findBySkuId("sku-mug-std").orElseThrow().reserved()).isEqualTo(2);
  }

  @Test
  void rejects_whenInsufficientStock() {
    var inventory = new InMemoryInventory();
    inventory.save(InventoryPosition.seed("sku-mug-std", 1));
    var fulfillments = new InMemoryFulfillments();
    var handler = new ReserveStockFromOrderPlacedHandler(inventory, fulfillments, "WH-1");

    FulfillmentOrder result =
        handler.handle(
            new OrderPlaced(
                "ord-2",
                "pay-2",
                new BigDecimal("98.00"),
                "CNY",
                List.of(new OrderPlaced.Line("sku-mug-std", 5, new BigDecimal("49.00")))));

    assertThat(result.status()).isEqualTo(FulfillmentStatus.REJECTED);
    assertThat(inventory.findBySkuId("sku-mug-std").orElseThrow().reserved()).isZero();
  }

  @Test
  void idempotent_whenFulfillmentExists() {
    var inventory = new InMemoryInventory();
    inventory.save(InventoryPosition.seed("sku-mug-std", 10));
    var fulfillments = new InMemoryFulfillments();
    fulfillments.save(FulfillmentOrder.allocate("ord-1", "WH-1"));
    var handler = new ReserveStockFromOrderPlacedHandler(inventory, fulfillments, "WH-1");

    FulfillmentOrder result =
        handler.handle(
            new OrderPlaced(
                "ord-1",
                "pay-1",
                new BigDecimal("49.00"),
                "CNY",
                List.of(new OrderPlaced.Line("sku-mug-std", 1, new BigDecimal("49.00")))));

    assertThat(result.status()).isEqualTo(FulfillmentStatus.ALLOCATED);
    assertThat(inventory.findBySkuId("sku-mug-std").orElseThrow().reserved()).isZero();
  }

  static final class InMemoryInventory implements InventoryRepository {
    private final Map<String, InventoryPosition> store = new HashMap<>();

    @Override
    public Optional<InventoryPosition> findBySkuId(String skuId) {
      return Optional.ofNullable(store.get(skuId));
    }

    @Override
    public void save(InventoryPosition position) {
      store.put(position.skuId(), position);
    }
  }

  static final class InMemoryFulfillments implements FulfillmentOrderRepository {
    private final Map<String, FulfillmentOrder> byOrder = new HashMap<>();

    @Override
    public Optional<FulfillmentOrder> findByOrderId(String orderId) {
      return Optional.ofNullable(byOrder.get(orderId));
    }

    @Override
    public void save(FulfillmentOrder order) {
      byOrder.put(order.orderId(), order);
    }
  }
}
