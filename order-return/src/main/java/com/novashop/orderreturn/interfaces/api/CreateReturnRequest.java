package com.novashop.orderreturn.interfaces.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public record CreateReturnRequest(
    @NotBlank String id,
    @NotBlank String paymentId,
    @NotBlank String refundableTotal,
    @NotEmpty @Valid List<ReturnLineRequest> lines,
    Set<String> alreadyApprovedLineIds) {

  public record ReturnLineRequest(@NotBlank String orderLineId, @NotNull Integer remainingRefundableQty) {}
}
