package com.novashop.commerce.salesorder.infrastructure.persistence;

import com.novashop.commerce.salesorder.domain.money.Money;
import com.novashop.commerce.salesorder.domain.order.OrderLine;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.ArrayList;
import java.util.List;

final class SalesOrderMapper {

  private SalesOrderMapper() {}

  static SalesOrder toDomain(SalesOrderEntity entity) {
    List<OrderLine> lines =
        entity.getLines().stream()
            .map(
                l ->
                    new OrderLine(
                        l.getLineId(),
                        l.getSkuId(),
                        l.getSkuName(),
                        l.getQuantity(),
                        Money.of(l.getUnitPrice(), l.getCurrency())))
            .toList();
    return SalesOrder.reconstitute(
        entity.getOrderId(),
        entity.getPaymentId(),
        lines,
        Money.of(entity.getTotalAmount(), entity.getCurrency()),
        Money.of(entity.getRefundableTotal(), entity.getCurrency()));
  }

  static SalesOrderEntity toEntity(SalesOrder order) {
    SalesOrderEntity entity =
        new SalesOrderEntity(
            order.orderId(),
            order.paymentId(),
            order.total().amount(),
            order.refundableTotal().amount(),
            order.total().currency());
    List<OrderLineEntity> lines = new ArrayList<>();
    for (OrderLine line : order.lines()) {
      lines.add(
          new OrderLineEntity(
              order.orderId() + ":" + line.lineId(),
              line.lineId(),
              line.skuId(),
              line.skuName(),
              line.quantity(),
              line.unitPrice().amount(),
              line.unitPrice().currency(),
              entity));
    }
    entity.setLines(lines);
    return entity;
  }
}
