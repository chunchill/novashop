package com.novashop.commerce.salesorder.interfaces.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
    String orderId, @NotEmpty @Valid List<LineRequest> lines) {

  public record LineRequest(@NotBlank String skuId, @Min(1) int quantity) {}
}
