package com.novashop.orderreturn.unit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.orderreturn.application.ApplyRefundTerminalHandler;
import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ApproveCommand;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequestException;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import com.novashop.platform.events.RefundTerminal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApplyRefundTerminalHandlerTest {

  @Test
  void projects_terminal_status_onto_return() {
    FakeRepo repo = new FakeRepo();
    ReturnRequest rr =
        ReturnRequest.pending(
            "ret-1", "pay-1", Money.cny("100.00"), List.of(new ReturnLine("L1", 1)), Set.of());
    rr.approve(
        new ApproveCommand(
            Set.of("L1"),
            Money.cny("100.00"),
            "FULL",
            "ok",
            "agent-1",
            ActorRole.AGENT,
            Money.cny("500.00")));
    repo.save(rr);

    var sut = new ApplyRefundTerminalHandler(repo);
    sut.handle(new RefundTerminal(rr.refundId(), "ret-1", "SUCCEEDED", null));

    assertThat(repo.findById("ret-1").orElseThrow().refundStatus()).isEqualTo("SUCCEEDED");
  }

  @Test
  void rejects_refund_id_mismatch() {
    FakeRepo repo = new FakeRepo();
    ReturnRequest rr =
        ReturnRequest.pending(
            "ret-1", "pay-1", Money.cny("100.00"), List.of(new ReturnLine("L1", 1)), Set.of());
    rr.approve(
        new ApproveCommand(
            Set.of("L1"),
            Money.cny("100.00"),
            "FULL",
            "ok",
            "agent-1",
            ActorRole.AGENT,
            Money.cny("500.00")));
    repo.save(rr);

    var sut = new ApplyRefundTerminalHandler(repo);
    assertThatThrownBy(
            () -> sut.handle(new RefundTerminal("other-rf", "ret-1", "FAILED", "x")))
        .isInstanceOf(ReturnRequestException.class);
  }

  static final class FakeRepo implements ReturnRequestRepository {
    private final Map<String, ReturnRequest> store = new HashMap<>();

    @Override
    public Optional<ReturnRequest> findById(String id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(ReturnRequest returnRequest) {
      store.put(returnRequest.id(), returnRequest);
    }

    @Override
    public List<ReturnRequest> findByStatus(ReturnStatus status) {
      return store.values().stream().filter(r -> r.status() == status).toList();
    }
  }
}
