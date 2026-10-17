package com.novashop.paymentrefund.unit.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.paymentrefund.application.ExecuteRefundHandler;
import com.novashop.paymentrefund.application.RecordRefundResultHandler;
import com.novashop.paymentrefund.application.RetryExternalExecutionHandler;
import com.novashop.paymentrefund.application.StartRefundFromReturnApprovedHandler;
import com.novashop.paymentrefund.application.port.PspRefundClient;
import com.novashop.paymentrefund.application.port.PspRefundResult;
import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.application.port.RefundTerminalOutboxPort;
import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.domain.refund.ExternalExecutionStatus;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.domain.refund.RefundStatus;
import com.novashop.platform.events.RefundTerminal;
import com.novashop.platform.events.ReturnApproved;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RefundApplicationHandlersTest {

  @Test
  void start_from_event_creates_pending_without_calling_psp() {
    var repo = new InMemoryRefundRepository();
    var psp = new FakePsp();
    var sut = new StartRefundFromReturnApprovedHandler(repo);

    sut.handle(sampleReturnApproved("rf-1"));

    assertThat(repo.get("rf-1").status()).isEqualTo(RefundStatus.PENDING_EXTERNAL_EXECUTION);
    assertThat(repo.get("rf-1").latestExecution().attemptCount()).isZero();
    assertThat(psp.calls()).isZero();
  }

  @Test
  void execute_calls_psp_with_refund_id_as_idempotency_key() {
    var repo = new InMemoryRefundRepository();
    var psp = new FakePsp();
    psp.nextResult = PspRefundResult.pendingCallback();
    var outbox = new FakeOutbox();
    seedPending(repo, "rf-2");

    new ExecuteRefundHandler(repo, psp, outbox).handle("rf-2");

    assertThat(psp.calls()).isEqualTo(1);
    assertThat(psp.lastIdempotencyKey()).isEqualTo("rf-2");
    assertThat(psp.lastRefundId()).isEqualTo("rf-2");
    assertThat(repo.get("rf-2").status()).isEqualTo(RefundStatus.PENDING_EXTERNAL_EXECUTION);
    assertThat(repo.get("rf-2").latestExecution().status())
        .isEqualTo(ExternalExecutionStatus.IN_PROGRESS);
    assertThat(outbox.events()).isEmpty();
  }

  @Test
  void execute_sync_success_records_result_and_publishes_terminal() {
    var repo = new InMemoryRefundRepository();
    var psp = new FakePsp();
    psp.nextResult = PspRefundResult.succeeded();
    var outbox = new FakeOutbox();
    seedPending(repo, "rf-sync");

    new ExecuteRefundHandler(repo, psp, outbox).handle("rf-sync");

    assertThat(repo.get("rf-sync").status()).isEqualTo(RefundStatus.SUCCEEDED);
    assertThat(outbox.last()).isInstanceOf(RefundTerminal.class);
    assertThat(((RefundTerminal) outbox.last()).status()).isEqualTo("SUCCEEDED");
    assertThat(((RefundTerminal) outbox.last()).refundId()).isEqualTo("rf-sync");
  }

  @Test
  void record_result_publishes_terminal_event() {
    var repo = new InMemoryRefundRepository();
    var outbox = new FakeOutbox();
    var refund = seedPending(repo, "rf-3");
    refund.recordExecutionAttempt();
    repo.save(refund);

    new RecordRefundResultHandler(repo, outbox).handle("rf-3", true, null);

    assertThat(repo.get("rf-3").status()).isEqualTo(RefundStatus.SUCCEEDED);
    assertThat(outbox.last()).isInstanceOf(RefundTerminal.class);
    var terminal = (RefundTerminal) outbox.last();
    assertThat(terminal.refundId()).isEqualTo("rf-3");
    assertThat(terminal.returnRequestId()).isEqualTo("ret-1");
    assertThat(terminal.status()).isEqualTo("SUCCEEDED");
    assertThat(terminal.failureReason()).isNull();
  }

  @Test
  void retry_then_re_executes_via_psp() {
    var repo = new InMemoryRefundRepository();
    var psp = new FakePsp();
    psp.nextResult = PspRefundResult.failed("timeout");
    var outbox = new FakeOutbox();
    seedPending(repo, "rf-4");

    var execute = new ExecuteRefundHandler(repo, psp, outbox);
    execute.handle("rf-4");
    assertThat(repo.get("rf-4").status()).isEqualTo(RefundStatus.FAILED);
    assertThat(psp.calls()).isEqualTo(1);

    psp.nextResult = PspRefundResult.succeeded();
    new RetryExternalExecutionHandler(repo, execute).handle("rf-4");

    assertThat(psp.calls()).isEqualTo(2);
    assertThat(psp.lastIdempotencyKey()).isEqualTo("rf-4");
    assertThat(repo.get("rf-4").status()).isEqualTo(RefundStatus.SUCCEEDED);
    assertThat(repo.get("rf-4").latestExecution().attemptCount()).isEqualTo(2);
    assertThat(outbox.events()).hasSize(2);
  }

  private static ReturnApproved sampleReturnApproved(String refundId) {
    return new ReturnApproved(
        refundId,
        "ret-1",
        "pay-1",
        new BigDecimal("100.00"),
        "CNY",
        List.of("L1"),
        "u1",
        "AGENT",
        "damaged",
        "FULL",
        refundId);
  }

  private static Refund seedPending(RefundRepository repo, String refundId) {
    var refund =
        Refund.createPending(refundId, "ret-1", "pay-1", Money.cny("100.00"), refundId);
    repo.save(refund);
    return refund;
  }

  static final class InMemoryRefundRepository implements RefundRepository {
    private final Map<String, Refund> store = new HashMap<>();

    @Override
    public Optional<Refund> findById(String id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Refund refund) {
      store.put(refund.id(), refund);
    }

    Refund get(String id) {
      return store.get(id);
    }
  }

  static final class FakePsp implements PspRefundClient {
    private int calls;
    private String lastRefundId;
    private String lastIdempotencyKey;
    PspRefundResult nextResult = PspRefundResult.pendingCallback();

    @Override
    public PspRefundResult refund(
        String refundId, String idempotencyKey, Money amount, String paymentId) {
      calls++;
      lastRefundId = refundId;
      lastIdempotencyKey = idempotencyKey;
      return nextResult;
    }

    int calls() {
      return calls;
    }

    String lastRefundId() {
      return lastRefundId;
    }

    String lastIdempotencyKey() {
      return lastIdempotencyKey;
    }
  }

  static final class FakeOutbox implements RefundTerminalOutboxPort {
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

    List<Object> events() {
      return List.copyOf(events);
    }
  }
}
