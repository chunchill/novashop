package com.novashop.fulfillment.application.port;

import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import java.util.Optional;

public interface InventoryRepository {

  Optional<InventoryPosition> findBySkuId(String skuId);

  void save(InventoryPosition position);
}
