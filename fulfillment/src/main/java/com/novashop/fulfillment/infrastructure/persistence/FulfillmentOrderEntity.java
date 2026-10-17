package com.novashop.fulfillment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfillment_order")
public class FulfillmentOrderEntity {

  @Id
  @Column(name = "id", nullable = false, length = 64)
  private String id;

  @Column(name = "order_id", nullable = false, length = 64, unique = true)
  private String orderId;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "warehouse_code", length = 64)
  private String warehouseCode;

  @Column(name = "reject_reason", length = 512)
  private String rejectReason;

  protected FulfillmentOrderEntity() {}

  public FulfillmentOrderEntity(
      String id, String orderId, String status, String warehouseCode, String rejectReason) {
    this.id = id;
    this.orderId = orderId;
    this.status = status;
    this.warehouseCode = warehouseCode;
    this.rejectReason = rejectReason;
  }

  public String getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getStatus() {
    return status;
  }

  public String getWarehouseCode() {
    return warehouseCode;
  }

  public String getRejectReason() {
    return rejectReason;
  }
}
