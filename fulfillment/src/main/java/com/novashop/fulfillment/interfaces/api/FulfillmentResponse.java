package com.novashop.fulfillment.interfaces.api;

import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;

public record FulfillmentResponse(
    String id, String orderId, String status, String warehouseCode, String rejectReason) {

  public static FulfillmentResponse from(FulfillmentOrder o) {
    return new FulfillmentResponse(
        o.id(), o.orderId(), o.status().name(), o.warehouseCode(), o.rejectReason());
  }
}
