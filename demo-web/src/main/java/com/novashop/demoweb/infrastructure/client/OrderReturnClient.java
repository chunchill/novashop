package com.novashop.demoweb.infrastructure.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderReturnClient {

  private final RestClient client;

  public OrderReturnClient(@Qualifier("orderReturnRestClient") RestClient client) {
    this.client = Objects.requireNonNull(client, "client");
  }

  public String createReturn(CreateReturnDto request) {
    Map<String, String> body =
        client
            .post()
            .uri("/returns")
            .body(request)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    return body == null ? null : body.get("id");
  }

  public List<ReturnDto> listPending() {
    ReturnDto[] body = client.get().uri("/returns/pending").retrieve().body(ReturnDto[].class);
    return body == null ? List.of() : Arrays.asList(body);
  }

  public ReturnDto getReturn(String id) {
    return client.get().uri("/returns/{id}", id).retrieve().body(ReturnDto.class);
  }

  public ApproveResponseDto approve(
      String returnId, ApproveReturnDto body, String actorId, String actorRole) {
    return client
        .post()
        .uri("/returns/{id}/approve", returnId)
        .header("X-Actor-Id", actorId)
        .header("X-Actor-Role", actorRole)
        .body(body)
        .retrieve()
        .body(ApproveResponseDto.class);
  }

  public record CreateReturnDto(
      String id,
      String paymentId,
      String refundableTotal,
      List<ReturnLineDto> lines,
      Set<String> alreadyApprovedLineIds) {}

  public record ReturnLineDto(String orderLineId, Integer remainingRefundableQty) {}

  public record ReturnDto(
      String id,
      String paymentId,
      String status,
      String refundableTotal,
      String currency,
      List<ReturnLineViewDto> lines,
      Set<String> refundedLineIds,
      String refundId,
      String refundStatus,
      String actorId,
      String actorRole,
      String reason,
      String resolutionCode,
      String refundAmount) {}

  public record ReturnLineViewDto(String orderLineId, int remainingRefundableQty) {}

  public record ApproveReturnDto(
      Set<String> lineIds, String refundAmount, String resolutionCode, String reason) {}

  public record ApproveResponseDto(String returnRequestId, String refundId) {}
}
