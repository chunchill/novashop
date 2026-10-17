package com.novashop.fulfillment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_position")
public class InventoryPositionEntity {

  @Id
  @Column(name = "sku_id", nullable = false, length = 64)
  private String skuId;

  @Column(name = "on_hand", nullable = false)
  private int onHand;

  @Column(name = "reserved", nullable = false)
  private int reserved;

  protected InventoryPositionEntity() {}

  public InventoryPositionEntity(String skuId, int onHand, int reserved) {
    this.skuId = skuId;
    this.onHand = onHand;
    this.reserved = reserved;
  }

  public String getSkuId() {
    return skuId;
  }

  public int getOnHand() {
    return onHand;
  }

  public int getReserved() {
    return reserved;
  }

  public void setOnHand(int onHand) {
    this.onHand = onHand;
  }

  public void setReserved(int reserved) {
    this.reserved = reserved;
  }
}
