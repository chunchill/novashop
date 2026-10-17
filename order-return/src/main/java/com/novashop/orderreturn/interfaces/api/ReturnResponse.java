package com.novashop.orderreturn.interfaces.api;

import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import java.util.List;
import java.util.Set;

public record ReturnResponse(
    String id,
    String paymentId,
    String status,
    String refundableTotal,
    String currency,
    List<LineResponse> lines,
    Set<String> refundedLineIds,
    String refundId,
    String refundStatus,
    String actorId,
    String actorRole,
    String reason,
    String resolutionCode,
    String refundAmount) {

  public static ReturnResponse from(ReturnRequest rr) {
    return new ReturnResponse(
        rr.id(),
        rr.paymentId(),
        rr.status().name(),
        rr.refundableTotal().amount().toPlainString(),
        rr.refundableTotal().currency(),
        rr.lines().stream().map(LineResponse::from).toList(),
        rr.refundedLineIds(),
        rr.refundId(),
        rr.refundStatus(),
        rr.actorId(),
        rr.actorRole() == null ? null : rr.actorRole().name(),
        rr.reason(),
        rr.resolutionCode(),
        rr.refundAmount() == null ? null : rr.refundAmount().amount().toPlainString());
  }

  public record LineResponse(String orderLineId, int remainingRefundableQty) {
    static LineResponse from(ReturnLine line) {
      return new LineResponse(line.orderLineId(), line.remainingRefundableQty());
    }
  }
}
