package com.novashop.demoweb.infrastructure.client;

import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class FulfillmentClient {

  private final RestClient client;

  public FulfillmentClient(@Qualifier("fulfillmentRestClient") RestClient client) {
    this.client = Objects.requireNonNull(client, "client");
  }

  public Optional<FulfillmentDto> getByOrderId(String orderId) {
    try {
      return Optional.ofNullable(
          client
              .get()
              .uri("/fulfillments/by-order/{orderId}", orderId)
              .retrieve()
              .body(FulfillmentDto.class));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<InventoryDto> getInventory(String skuId) {
    try {
      return Optional.ofNullable(
          client.get().uri("/inventory/{skuId}", skuId).retrieve().body(InventoryDto.class));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public record FulfillmentDto(
      String id, String orderId, String status, String warehouseCode, String rejectReason) {}

  public record InventoryDto(String skuId, int onHand, int reserved, int available) {}
}
