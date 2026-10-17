package com.novashop.commerce.unit.salesorder.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.commerce.salesorder.domain.money.Money;
import com.novashop.commerce.salesorder.domain.order.OrderLine;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.List;
import org.junit.jupiter.api.Test;

class SalesOrderTest {

  @Test
  void place_mintsPaymentId_andSetsRefundableEqualToTotal() {
    var order =
        SalesOrder.place(
            "ord-1",
            List.of(
                new OrderLine("L1", "sku-a", "A", 2, Money.cny("50.00")),
                new OrderLine("L2", "sku-b", "B", 1, Money.cny("20.00"))));

    assertThat(order.orderId()).isEqualTo("ord-1");
    assertThat(order.paymentId()).startsWith("pay-");
    assertThat(order.total().amount()).isEqualByComparingTo("120.00");
    assertThat(order.refundableTotal()).isEqualTo(order.total());
    assertThat(order.lines()).hasSize(2);
  }

  @Test
  void place_rejectsEmptyLines() {
    assertThatThrownBy(() -> SalesOrder.place("ord-1", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
