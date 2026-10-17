package com.novashop.commerce.salesorder.interfaces.api;

import com.novashop.commerce.salesorder.domain.order.OrderLine;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.List;

public record OrderResponse(
    String orderId,
    String paymentId,
    String total,
    String refundableTotal,
    String currency,
    List<LineResponse> lines) {

  public static OrderResponse from(SalesOrder order) {
    return new OrderResponse(
        order.orderId(),
        order.paymentId(),
        order.total().amount().toPlainString(),
        order.refundableTotal().amount().toPlainString(),
        order.total().currency(),
        order.lines().stream().map(LineResponse::from).toList());
  }

  public record LineResponse(
      String lineId, String skuId, String skuName, int quantity, String unitPrice) {
    static LineResponse from(OrderLine line) {
      return new LineResponse(
          line.lineId(),
          line.skuId(),
          line.skuName(),
          line.quantity(),
          line.unitPrice().amount().toPlainString());
    }
  }
}
