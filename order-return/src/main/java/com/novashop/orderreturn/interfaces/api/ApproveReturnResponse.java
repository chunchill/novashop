package com.novashop.orderreturn.interfaces.api;

import com.novashop.orderreturn.application.ApproveResult;

public record ApproveReturnResponse(String returnRequestId, String refundId) {

  public static ApproveReturnResponse from(ApproveResult result) {
    return new ApproveReturnResponse(result.returnRequestId(), result.refundId());
  }
}
