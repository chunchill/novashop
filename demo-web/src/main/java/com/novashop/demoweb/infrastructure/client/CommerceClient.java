package com.novashop.demoweb.infrastructure.client;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CommerceClient {

  private final RestClient client;

  public CommerceClient(@Qualifier("commerceRestClient") RestClient client) {
    this.client = Objects.requireNonNull(client, "client");
  }

  public List<ProductDto> listProducts() {
    ProductDto[] body = client.get().uri("/products").retrieve().body(ProductDto[].class);
    return body == null ? List.of() : Arrays.asList(body);
  }

  public OrderDto createOrder(CreateOrderDto request) {
    return client
        .post()
        .uri("/orders")
        .body(request)
        .retrieve()
        .body(OrderDto.class);
  }

  public OrderDto getOrder(String orderId) {
    return client.get().uri("/orders/{id}", orderId).retrieve().body(OrderDto.class);
  }

  public record ProductDto(
      String productId, String name, String description, List<SkuDto> skus) {}

  public record SkuDto(String skuId, String name, String unitPrice, String currency) {}

  public record CreateOrderDto(String orderId, List<LineDto> lines) {}

  public record LineDto(String skuId, int quantity) {}

  public record OrderDto(
      String orderId,
      String paymentId,
      String total,
      String refundableTotal,
      String currency,
      List<OrderLineDto> lines) {}

  public record OrderLineDto(
      String lineId, String skuId, String skuName, int quantity, String unitPrice) {}
}
