package com.novashop.fulfillment.application;

import com.novashop.fulfillment.application.port.InventoryRepository;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import java.util.Objects;
import java.util.Optional;

public class GetInventoryHandler {

  private final InventoryRepository inventoryRepository;

  public GetInventoryHandler(InventoryRepository inventoryRepository) {
    this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "inventoryRepository");
  }

  public Optional<InventoryPosition> handle(String skuId) {
    return inventoryRepository.findBySkuId(skuId);
  }
}
