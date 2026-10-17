package com.novashop.fulfillment.application;

import com.novashop.fulfillment.application.port.FulfillmentOrderRepository;
import com.novashop.fulfillment.application.port.InventoryRepository;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;
import com.novashop.fulfillment.domain.inventory.InventoryException;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import com.novashop.platform.events.OrderPlaced;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * On {@link OrderPlaced}: reserve inventory for each line and create an ALLOCATED fulfillment, or
 * REJECTED if stock is insufficient.
 */
public class ReserveStockFromOrderPlacedHandler {

  private static final Logger log = LoggerFactory.getLogger(ReserveStockFromOrderPlacedHandler.class);

  private final InventoryRepository inventoryRepository;
  private final FulfillmentOrderRepository fulfillmentOrderRepository;
  private final String warehouseCode;

  public ReserveStockFromOrderPlacedHandler(
      InventoryRepository inventoryRepository,
      FulfillmentOrderRepository fulfillmentOrderRepository,
      String warehouseCode) {
    this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository");
    this.fulfillmentOrderRepository =
        Objects.requireNonNull(fulfillmentOrderRepository, "fulfillmentOrderRepository");
    this.warehouseCode = Objects.requireNonNull(warehouseCode, "warehouseCode");
  }

  public FulfillmentOrder handle(OrderPlaced event) {
    Objects.requireNonNull(event, "event");
    var existing = fulfillmentOrderRepository.findByOrderId(event.orderId());
    if (existing.isPresent()) {
      log.info("fulfillment already exists for orderId={}", event.orderId());
      return existing.get();
    }

    List<InventoryPosition> toSave = new ArrayList<>();
    try {
      // Load + validate availability first (no partial mutate on reject).
      for (OrderPlaced.Line line : event.lines()) {
        InventoryPosition position =
            inventoryRepository
                .findBySkuId(line.skuId())
                .orElseThrow(
                    () -> new InventoryException("unknown sku inventory: " + line.skuId()));
        if (position.available() < line.quantity()) {
          throw new InventoryException(
              "insufficient stock for sku "
                  + line.skuId()
                  + ": need "
                  + line.quantity()
                  + ", available "
                  + position.available());
        }
        toSave.add(position);
      }
      for (int i = 0; i < event.lines().size(); i++) {
        toSave.get(i).reserve(event.lines().get(i).quantity());
      }
    } catch (InventoryException ex) {
      log.warn("stock reservation rejected orderId={}: {}", event.orderId(), ex.getMessage());
      FulfillmentOrder rejected = FulfillmentOrder.reject(event.orderId(), ex.getMessage());
      fulfillmentOrderRepository.save(rejected);
      return rejected;
    }

    for (InventoryPosition position : toSave) {
      inventoryRepository.save(position);
    }
    FulfillmentOrder allocated = FulfillmentOrder.allocate(event.orderId(), warehouseCode);
    fulfillmentOrderRepository.save(allocated);
    log.info(
        "stock reserved and fulfillment ALLOCATED orderId={} fulfillmentId={}",
        event.orderId(),
        allocated.id());
    return allocated;
  }
}
