package com.novashop.orderreturn.unit.domain.returnrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ApproveCommand;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequestException;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReturnRequestTest {

  @Test
  void agent_may_approve_up_to_500() {
    var request = pendingReturn(Money.cny("400.00"), line("L1", 1));
    var decision =
        request.approve(
            new ApproveCommand(
                Set.of("L1"),
                Money.cny("400.00"),
                "FULL",
                "damaged",
                "agent-1",
                ActorRole.AGENT,
                Money.cny("500.00")));

    assertThat(request.status()).isEqualTo(ReturnStatus.APPROVED);
    assertThat(decision.refundId()).isNotBlank();
  }

  @Test
  void agent_may_not_approve_over_500() {
    var request = pendingReturn(Money.cny("600.00"), line("L1", 1));

    assertThatThrownBy(
            () ->
                request.approve(
                    new ApproveCommand(
                        Set.of("L1"),
                        Money.cny("600.00"),
                        "FULL",
                        "damaged",
                        "agent-1",
                        ActorRole.AGENT,
                        Money.cny("500.00"))))
        .isInstanceOf(ReturnRequestException.class);
  }

  @Test
  void supervisor_may_approve_over_500() {
    var request = pendingReturn(Money.cny("600.00"), line("L1", 1));
    var decision =
        request.approve(
            new ApproveCommand(
                Set.of("L1"),
                Money.cny("600.00"),
                "FULL",
                "damaged",
                "sup-1",
                ActorRole.SUPERVISOR,
                Money.cny("500.00")));

    assertThat(request.status()).isEqualTo(ReturnStatus.APPROVED);
    assertThat(decision.refundId()).isNotBlank();
  }

  @Test
  void cannot_approve_when_not_pending() {
    var request = pendingReturn(Money.cny("100.00"), line("L1", 1));
    request.approve(
        new ApproveCommand(
            Set.of("L1"),
            Money.cny("100.00"),
            "FULL",
            "ok",
            "agent-1",
            ActorRole.AGENT,
            Money.cny("500.00")));

    assertThatThrownBy(
            () ->
                request.approve(
                    new ApproveCommand(
                        Set.of("L1"),
                        Money.cny("100.00"),
                        "FULL",
                        "again",
                        "agent-1",
                        ActorRole.AGENT,
                        Money.cny("500.00"))))
        .isInstanceOf(ReturnRequestException.class);
  }

  @Test
  void cannot_exceed_refundable_amount() {
    var request = pendingReturn(Money.cny("200.00"), line("L1", 1));

    assertThatThrownBy(
            () ->
                request.approve(
                    new ApproveCommand(
                        Set.of("L1"),
                        Money.cny("200.01"),
                        "FULL",
                        "too much",
                        "agent-1",
                        ActorRole.AGENT,
                        Money.cny("500.00"))))
        .isInstanceOf(ReturnRequestException.class);
  }

  @Test
  void cannot_approve_same_line_twice_across_requests_on_snapshot() {
    var request =
        ReturnRequest.pending(
            "ret-dup",
            "pay-1",
            Money.cny("100.00"),
            List.of(line("L1", 1)),
            Set.of("L1"));

    assertThatThrownBy(
            () ->
                request.approve(
                    new ApproveCommand(
                        Set.of("L1"),
                        Money.cny("100.00"),
                        "FULL",
                        "dup",
                        "agent-1",
                        ActorRole.AGENT,
                        Money.cny("500.00"))))
        .isInstanceOf(ReturnRequestException.class);
  }

  @Test
  void cannot_approve_line_with_zero_remaining_qty() {
    var request = pendingReturn(Money.cny("100.00"), line("L1", 0));

    assertThatThrownBy(
            () ->
                request.approve(
                    new ApproveCommand(
                        Set.of("L1"),
                        Money.cny("100.00"),
                        "FULL",
                        "no qty",
                        "agent-1",
                        ActorRole.AGENT,
                        Money.cny("500.00"))))
        .isInstanceOf(ReturnRequestException.class)
        .hasMessageContaining("remaining refundable qty");
  }

  @Test
  void approve_mints_refund_id() {
    var request = pendingReturn(Money.cny("150.00"), line("L1", 1));
    var decision =
        request.approve(
            new ApproveCommand(
                Set.of("L1"),
                Money.cny("150.00"),
                "FULL",
                "mint",
                "agent-1",
                ActorRole.AGENT,
                Money.cny("500.00")));

    assertThat(decision.refundId()).isNotBlank();
    assertThat(UUID.fromString(decision.refundId())).isNotNull();
  }

  @Test
  void apply_refund_terminal_projects_status() {
    var request = pendingReturn(Money.cny("100.00"), line("L1", 1));
    request.approve(
        new ApproveCommand(
            Set.of("L1"),
            Money.cny("100.00"),
            "FULL",
            "ok",
            "agent-1",
            ActorRole.AGENT,
            Money.cny("500.00")));

    request.applyRefundTerminal("SUCCEEDED");

    assertThat(request.refundStatus()).isEqualTo("SUCCEEDED");
  }

  @Test
  void apply_refund_terminal_requires_approved() {
    var request = pendingReturn(Money.cny("100.00"), line("L1", 1));

    assertThatThrownBy(() -> request.applyRefundTerminal("SUCCEEDED"))
        .isInstanceOf(ReturnRequestException.class);
  }

  private static ReturnRequest pendingReturn(Money refundableTotal, ReturnLine... lines) {
    return ReturnRequest.pending("ret-1", "pay-1", refundableTotal, List.of(lines), Set.of());
  }

  private static ReturnLine line(String orderLineId, int remainingRefundableQty) {
    return new ReturnLine(orderLineId, remainingRefundableQty);
  }
}
