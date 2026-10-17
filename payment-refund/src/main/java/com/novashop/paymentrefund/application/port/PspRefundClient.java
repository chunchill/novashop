package com.novashop.paymentrefund.application.port;

import com.novashop.paymentrefund.domain.money.Money;

public interface PspRefundClient {

  /**
   * Initiates a PSP refund. Use {@code idempotencyKey} (refund id) for safe retries.
   *
   * @return sync success/failure, or pending when the PSP will complete via callback
   */
  PspRefundResult refund(String refundId, String idempotencyKey, Money amount, String paymentId);
}
