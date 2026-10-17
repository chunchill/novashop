package com.novashop.paymentrefund.unit.domain.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.domain.refund.ExternalExecutionStatus;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.domain.refund.RefundException;
import com.novashop.paymentrefund.domain.refund.RefundStatus;
import org.junit.jupiter.api.Test;

class RefundTest {

  @Test
  void create_pending_before_psp_success() {
    var refund = pendingRefund("rf-1");

    assertThat(refund.status()).isEqualTo(RefundStatus.PENDING_EXTERNAL_EXECUTION);
    assertThat(refund.latestExecution().attemptCount()).isZero();
    assertThat(refund.latestExecution().status())
        .isEqualTo(ExternalExecutionStatus.PENDING);
    assertThat(refund.latestExecution().idempotencyKey()).isEqualTo("rf-1");
  }

  @Test
  void cannot_mark_succeeded_without_execution_result_path() {
    var refund = pendingRefund("rf-2");

    refund.recordExecutionAttempt();

    assertThat(refund.status()).isEqualTo(RefundStatus.PENDING_EXTERNAL_EXECUTION);
    assertThat(refund.status()).isNotEqualTo(RefundStatus.SUCCEEDED);
    assertThat(refund.latestExecution().status())
        .isNotEqualTo(ExternalExecutionStatus.SUCCEEDED);
  }

  @Test
  void record_result_success() {
    var refund = pendingRefund("rf-3");
    refund.recordExecutionAttempt();

    refund.recordResult(true, null);

    assertThat(refund.status()).isEqualTo(RefundStatus.SUCCEEDED);
    assertThat(refund.latestExecution().status())
        .isEqualTo(ExternalExecutionStatus.SUCCEEDED);
    assertThat(refund.latestExecution().lastError()).isNull();
  }

  @Test
  void record_result_failure_allows_retry() {
    var refund = pendingRefund("rf-4");
    refund.recordExecutionAttempt();
    assertThat(refund.latestExecution().attemptCount()).isEqualTo(1);

    refund.recordResult(false, "psp timeout");

    assertThat(refund.status()).isEqualTo(RefundStatus.FAILED);
    assertThat(refund.latestExecution().status())
        .isEqualTo(ExternalExecutionStatus.FAILED);
    assertThat(refund.latestExecution().lastError()).isEqualTo("psp timeout");

    refund.retry();

    assertThat(refund.status()).isEqualTo(RefundStatus.PENDING_EXTERNAL_EXECUTION);
    assertThat(refund.latestExecution().attemptCount()).isEqualTo(2);
    assertThat(refund.latestExecution().status())
        .isEqualTo(ExternalExecutionStatus.PENDING);
    assertThat(refund.latestExecution().lastError()).isNull();
  }

  @Test
  void duplicate_record_result_same_idempotency_is_noop() {
    var refund = pendingRefund("rf-5");
    refund.recordExecutionAttempt();
    refund.recordResult(true, null);

    refund.recordResult(true, null);
    refund.recordResult(false, "should not apply");

    assertThat(refund.status()).isEqualTo(RefundStatus.SUCCEEDED);
    assertThat(refund.latestExecution().status())
        .isEqualTo(ExternalExecutionStatus.SUCCEEDED);
    assertThat(refund.latestExecution().lastError()).isNull();
  }

  @Test
  void retry_when_not_failed_is_rejected() {
    var refund = pendingRefund("rf-6");

    assertThatThrownBy(refund::retry).isInstanceOf(RefundException.class);
  }

  private static Refund pendingRefund(String refundId) {
    return Refund.createPending(
        refundId, "ret-1", "pay-1", Money.cny("100.00"), refundId);
  }
}
