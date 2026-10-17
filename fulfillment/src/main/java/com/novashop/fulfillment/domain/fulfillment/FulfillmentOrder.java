package com.novashop.fulfillment.domain.fulfillment;

import java.util.Objects;
import java.util.UUID;

/** Simple fulfillment record for a sales order (Phase 3 teaching stub). */
public final class FulfillmentOrder {

  private final String id;
  private final String orderId;
  private final FulfillmentStatus status;
  private final String warehouseCode;
  private final String rejectReason;

  private FulfillmentOrder(
      String id,
      String orderId,
      FulfillmentStatus status,
      String warehouseCode,
      String rejectReason) {
    this.id = Objects.requireNonNull(id, "id");
    this.orderId = Objects.requireNonNull(orderId, "orderId");
    this.status = Objects.requireNonNull(status, "status");
    this.warehouseCode = warehouseCode;
    this.rejectReason = rejectReason;
  }

  public static FulfillmentOrder allocate(String orderId, String warehouseCode) {
    Objects.requireNonNull(warehouseCode, "warehouseCode");
    return new FulfillmentOrder(
        "ful-" + UUID.randomUUID(), orderId, FulfillmentStatus.ALLOCATED, warehouseCode, null);
  }

  public static FulfillmentOrder reject(String orderId, String reason) {
    Objects.requireNonNull(reason, "reason");
    return new FulfillmentOrder(
        "ful-" + UUID.randomUUID(), orderId, FulfillmentStatus.REJECTED, null, reason);
  }

  public static FulfillmentOrder reconstitute(
      String id,
      String orderId,
      FulfillmentStatus status,
      String warehouseCode,
      String rejectReason) {
    return new FulfillmentOrder(id, orderId, status, warehouseCode, rejectReason);
  }

  public String id() {
    return id;
  }

  public String orderId() {
    return orderId;
  }

  public FulfillmentStatus status() {
    return status;
  }

  public String warehouseCode() {
    return warehouseCode;
  }

  public String rejectReason() {
    return rejectReason;
  }
}
