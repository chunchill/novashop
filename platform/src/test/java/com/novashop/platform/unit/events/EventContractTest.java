package com.novashop.platform.unit.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.platform.events.OrderPlaced;
import com.novashop.platform.events.RefundTerminal;
import com.novashop.platform.events.ReturnApproved;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventContractTest {

  @Test
  void orderPlaced_requiresOrderId() {
    assertThatThrownBy(
            () ->
                new OrderPlaced(
                    null,
                    "pay-1",
                    new BigDecimal("99.00"),
                    "CNY",
                    List.of(new OrderPlaced.Line("sku-1", 1, new BigDecimal("99.00")))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void orderPlaced_acceptsValidPayload() {
    var event =
        new OrderPlaced(
            "ord-1",
            "pay-1",
            new BigDecimal("198.00"),
            "CNY",
            List.of(
                new OrderPlaced.Line("sku-1", 2, new BigDecimal("99.00"))));

    assertThat(event.orderId()).isEqualTo("ord-1");
    assertThat(event.paymentId()).isEqualTo("pay-1");
    assertThat(event.totalAmount()).isEqualByComparingTo("198.00");
    assertThat(event.currency()).isEqualTo("CNY");
    assertThat(event.lines()).hasSize(1);
    assertThat(event.lines()).isUnmodifiable();
  }

  @Test
  void returnApproved_requiresRefundId() {
    assertThatThrownBy(
            () ->
                new ReturnApproved(
                    null,
                    "ret-1",
                    "pay-1",
                    new BigDecimal("100.00"),
                    "CNY",
                    List.of("line-1"),
                    "u1",
                    "AGENT",
                    "damaged",
                    "FULL",
                    "ret-1"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void returnApproved_acceptsValidPayload() {
    var event =
        new ReturnApproved(
            "rf-1",
            "ret-1",
            "pay-1",
            new BigDecimal("100.00"),
            "CNY",
            List.of("line-1"),
            "u1",
            "AGENT",
            "damaged",
            "FULL",
            "idem-1");

    assertThat(event.refundId()).isEqualTo("rf-1");
    assertThat(event.returnRequestId()).isEqualTo("ret-1");
    assertThat(event.paymentId()).isEqualTo("pay-1");
    assertThat(event.amount()).isEqualByComparingTo("100.00");
    assertThat(event.currency()).isEqualTo("CNY");
    assertThat(event.approvedLineIds()).containsExactly("line-1");
    assertThat(event.actorId()).isEqualTo("u1");
    assertThat(event.actorRole()).isEqualTo("AGENT");
    assertThat(event.reason()).isEqualTo("damaged");
    assertThat(event.resolutionCode()).isEqualTo("FULL");
    assertThat(event.idempotencyKey()).isEqualTo("idem-1");
  }

  @Test
  void returnApproved_copiesApprovedLineIds() {
    var lines = List.of("line-1", "line-2");
    var event =
        new ReturnApproved(
            "rf-1",
            "ret-1",
            "pay-1",
            new BigDecimal("50.00"),
            "CNY",
            lines,
            "u1",
            "SUPERVISOR",
            "wrong-size",
            "PARTIAL",
            "idem-2");

    assertThat(event.approvedLineIds()).isUnmodifiable();
    assertThat(event.approvedLineIds()).containsExactly("line-1", "line-2");
  }

  @Test
  void refundTerminal_requiresRefundId() {
    assertThatThrownBy(() -> new RefundTerminal(null, "ret-1", "SUCCEEDED", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refundTerminal_requiresReturnRequestId() {
    assertThatThrownBy(() -> new RefundTerminal("rf-1", null, "FAILED", "psp timeout"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refundTerminal_requiresStatus() {
    assertThatThrownBy(() -> new RefundTerminal("rf-1", "ret-1", null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refundTerminal_acceptsSucceeded() {
    var event = new RefundTerminal("rf-1", "ret-1", "SUCCEEDED", null);

    assertThat(event.refundId()).isEqualTo("rf-1");
    assertThat(event.returnRequestId()).isEqualTo("ret-1");
    assertThat(event.status()).isEqualTo("SUCCEEDED");
    assertThat(event.failureReason()).isNull();
  }

  @Test
  void refundTerminal_acceptsFailedWithReason() {
    var event = new RefundTerminal("rf-1", "ret-1", "FAILED", "psp declined");

    assertThat(event.status()).isEqualTo("FAILED");
    assertThat(event.failureReason()).isEqualTo("psp declined");
  }
}
