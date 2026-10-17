package com.novashop.orderreturn.unit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.orderreturn.application.ApproveReturnCommand;
import com.novashop.orderreturn.application.ApproveReturnHandler;
import com.novashop.orderreturn.application.CreateReturnCommand;
import com.novashop.orderreturn.application.CreateReturnHandler;
import com.novashop.orderreturn.application.ListPendingReturnsHandler;
import com.novashop.orderreturn.application.port.OutboxPort;
import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import com.novashop.platform.events.ReturnApproved;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApproveReturnHandlerTest {

  @Test
  void approve_writes_outbox_with_same_refund_id() {
    var repo = new InMemoryReturnRequestRepository();
    var outbox = new FakeOutboxPort();
    var sut = new ApproveReturnHandler(repo, outbox, Money.cny("500.00"));
    var id = seedPending(repo, Money.cny("200.00"));

    var result =
        sut.handle(
            id,
            new ApproveReturnCommand(Set.of("L1"), Money.cny("200.00"), "FULL", "damaged"),
            "u1",
            ActorRole.AGENT);

    assertThat(result.refundId()).isNotBlank();
    assertThat(outbox.last()).isInstanceOf(ReturnApproved.class);
    assertThat(((ReturnApproved) outbox.last()).refundId()).isEqualTo(result.refundId());
    assertThat(((ReturnApproved) outbox.last()).idempotencyKey()).isEqualTo(result.refundId());
    assertThat(repo.findById(id)).hasValueSatisfying(rr -> {
      assertThat(rr.status()).isEqualTo(ReturnStatus.APPROVED);
      assertThat(rr.refundId()).isEqualTo(result.refundId());
    });
  }

  @Test
  void approve_missing_return_throws() {
    var sut =
        new ApproveReturnHandler(
            new InMemoryReturnRequestRepository(), new FakeOutboxPort(), Money.cny("500.00"));

    assertThatThrownBy(
            () ->
                sut.handle(
                    "missing",
                    new ApproveReturnCommand(Set.of("L1"), Money.cny("10.00"), "FULL", "x"),
                    "u1",
                    ActorRole.AGENT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("missing");
  }

  @Test
  void create_and_list_pending() {
    var repo = new InMemoryReturnRequestRepository();
    var create = new CreateReturnHandler(repo);
    var list = new ListPendingReturnsHandler(repo);

    create.handle(
        new CreateReturnCommand(
            "ret-a",
            "pay-1",
            Money.cny("100.00"),
            List.of(new ReturnLine("L1", 1)),
            Set.of()));

    assertThat(list.handle()).extracting(ReturnRequest::id).containsExactly("ret-a");
  }

  private static String seedPending(ReturnRequestRepository repo, Money refundableTotal) {
    var id = "ret-1";
    repo.save(
        ReturnRequest.pending(
            id, "pay-1", refundableTotal, List.of(new ReturnLine("L1", 1)), Set.of()));
    return id;
  }

  static final class InMemoryReturnRequestRepository implements ReturnRequestRepository {
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
      return store.values().stream().filter(rr -> rr.status() == status).toList();
    }
  }

  static final class FakeOutboxPort implements OutboxPort {
    private final List<Object> events = new ArrayList<>();

    @Override
    public void append(Object event) {
      events.add(event);
    }

    Object last() {
      if (events.isEmpty()) {
        throw new IllegalStateException("outbox empty");
      }
      return events.get(events.size() - 1);
    }
  }
}
