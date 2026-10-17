package com.novashop.commerce.salesorder.application;

import java.util.List;

public record CreateOrderCommand(String orderId, List<Line> lines) {

  public record Line(String skuId, int quantity) {}
}
