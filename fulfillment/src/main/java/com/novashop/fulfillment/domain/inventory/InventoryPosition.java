package com.novashop.fulfillment.domain.inventory;

import java.util.Objects;

/** Stock position per SKU: on-hand quantity and reserved (allocated to orders). */
public final class InventoryPosition {

  private final String skuId;
  private int onHand;
  private int reserved;

  private InventoryPosition(String skuId, int onHand, int reserved) {
    this.skuId = Objects.requireNonNull(skuId, "skuId");
    if (onHand < 0 || reserved < 0) {
      throw new IllegalArgumentException("quantities must be non-negative");
    }
    if (reserved > onHand) {
      throw new IllegalArgumentException("reserved cannot exceed onHand");
    }
    this.onHand = onHand;
    this.reserved = reserved;
  }

  public static InventoryPosition seed(String skuId, int onHand) {
    return new InventoryPosition(skuId, onHand, 0);
  }

  public static InventoryPosition reconstitute(String skuId, int onHand, int reserved) {
    return new InventoryPosition(skuId, onHand, reserved);
  }

  public int available() {
    return onHand - reserved;
  }

  public void reserve(int quantity) {
    if (quantity <= 0) {
      throw new InventoryException("quantity must be positive");
    }
    if (available() < quantity) {
      throw new InventoryException(
          "insufficient stock for sku " + skuId + ": need " + quantity + ", available " + available());
    }
    reserved += quantity;
  }

  public String skuId() {
    return skuId;
  }

  public int onHand() {
    return onHand;
  }

  public int reserved() {
    return reserved;
  }
}
