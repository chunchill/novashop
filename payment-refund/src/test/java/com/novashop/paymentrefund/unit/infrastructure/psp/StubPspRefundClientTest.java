package com.novashop.paymentrefund.unit.infrastructure.psp;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.paymentrefund.application.port.PspRefundResult;
import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.infrastructure.psp.PspMode;
import com.novashop.paymentrefund.infrastructure.psp.StubPspRefundClient;
import org.junit.jupiter.api.Test;

class StubPspRefundClientTest {

  @Test
  void success_sync_returns_succeeded() {
    var client = new StubPspRefundClient("SUCCESS_SYNC");
    assertThat(client.refund("rf", "rf", Money.cny("10.00"), "pay").outcome())
        .isEqualTo(PspRefundResult.Outcome.SUCCEEDED);
  }

  @Test
  void fail_returns_failed() {
    var client = new StubPspRefundClient("FAIL");
    PspRefundResult result = client.refund("rf", "rf", Money.cny("10.00"), "pay");
    assertThat(result.outcome()).isEqualTo(PspRefundResult.Outcome.FAILED);
    assertThat(result.failureReason()).isNotBlank();
  }

  @Test
  void callback_later_returns_pending() {
    var client = new StubPspRefundClient("CALLBACK_LATER");
    assertThat(client.refund("rf", "rf", Money.cny("10.00"), "pay").outcome())
        .isEqualTo(PspRefundResult.Outcome.PENDING_CALLBACK);
  }
}
