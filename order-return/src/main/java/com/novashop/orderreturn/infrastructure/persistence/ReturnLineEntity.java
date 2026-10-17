package com.novashop.orderreturn.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ReturnLineEntity {

  @Column(name = "order_line_id", nullable = false)
  private String orderLineId;

  @Column(name = "remaining_refundable_qty", nullable = false)
  private int remainingRefundableQty;

  protected ReturnLineEntity() {}

  public ReturnLineEntity(String orderLineId, int remainingRefundableQty) {
    this.orderLineId = orderLineId;
    this.remainingRefundableQty = remainingRefundableQty;
  }

  public String getOrderLineId() {
    return orderLineId;
  }

  public int getRemainingRefundableQty() {
    return remainingRefundableQty;
  }
}
