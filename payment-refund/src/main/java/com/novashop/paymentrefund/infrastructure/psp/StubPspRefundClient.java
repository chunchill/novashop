package com.novashop.paymentrefund.infrastructure.psp;

import com.novashop.paymentrefund.application.port.PspRefundClient;
import com.novashop.paymentrefund.application.port.PspRefundResult;
import com.novashop.paymentrefund.domain.money.Money;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StubPspRefundClient implements PspRefundClient {

  private static final Logger log = LoggerFactory.getLogger(StubPspRefundClient.class);

  private final PspMode mode;

  public StubPspRefundClient(@Value("${novashop.psp.mode:SUCCESS_SYNC}") String mode) {
    this.mode = PspMode.valueOf(Objects.requireNonNull(mode, "mode").trim().toUpperCase());
  }

  @Override
  public PspRefundResult refund(
      String refundId, String idempotencyKey, Money amount, String paymentId) {
    log.info(
        "stub PSP refund mode={} refundId={} idempotencyKey={} amount={} paymentId={}",
        mode,
        refundId,
        idempotencyKey,
        amount,
        paymentId);
    return switch (mode) {
      case SUCCESS_SYNC -> PspRefundResult.succeeded();
      case FAIL -> PspRefundResult.failed("stub PSP failure");
      case CALLBACK_LATER -> PspRefundResult.pendingCallback();
    };
  }
}
