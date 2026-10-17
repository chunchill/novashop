package com.novashop.paymentrefund.interfaces.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PspRefundCallbackRequest(
    @NotBlank String refundId, @NotNull Boolean success, String failureReason) {}
