package com.novashop.orderreturn.domain.returnrequest;

import java.util.Objects;

public final class ReturnLine {

  private final String orderLineId;
  private final int remainingRefundableQty;

  public ReturnLine(String orderLineId, int remainingRefundableQty) {
    this.orderLineId = Objects.requireNonNull(orderLineId, "orderLineId");
    if (remainingRefundableQty < 0) {
      throw new IllegalArgumentException("remainingRefundableQty");
    }
    this.remainingRefundableQty = remainingRefundableQty;
  }

  public String orderLineId() {
    return orderLineId;
  }

  public int remainingRefundableQty() {
    return remainingRefundableQty;
  }
}
