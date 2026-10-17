package com.novashop.demoweb.infrastructure.client;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentRefundClient {

  private final RestClient client;

  public PaymentRefundClient(@Qualifier("paymentRefundRestClient") RestClient client) {
    this.client = Objects.requireNonNull(client, "client");
  }

  public RefundDto getRefund(String refundId) {
    return client.get().uri("/refunds/{id}", refundId).retrieve().body(RefundDto.class);
  }

  /** Teaching-only: simulate PSP callback success (POST /psp/callbacks/refunds). */
  public void simulatePspCallbackSuccess(String refundId) {
    client
        .post()
        .uri("/psp/callbacks/refunds")
        .body(new PspCallbackDto(refundId, true, null))
        .retrieve()
        .toBodilessEntity();
  }

  public record PspCallbackDto(String refundId, Boolean success, String failureReason) {}

  public record RefundDto(
      String id,
      String returnRequestId,
      String paymentId,
      String amount,
      String currency,
      String idempotencyKey,
      String status,
      ExternalExecutionDto latestExecution) {}

  public record ExternalExecutionDto(
      String idempotencyKey, int attemptCount, String status, String lastError) {}
}
