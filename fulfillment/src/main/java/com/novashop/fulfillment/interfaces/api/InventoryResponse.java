package com.novashop.fulfillment.interfaces.api;

import com.novashop.fulfillment.domain.inventory.InventoryPosition;

public record InventoryResponse(String skuId, int onHand, int reserved, int available) {

  public static InventoryResponse from(InventoryPosition p) {
    return new InventoryResponse(p.skuId(), p.onHand(), p.reserved(), p.available());
  }
}
