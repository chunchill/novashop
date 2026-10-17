package com.novashop.orderreturn.interfaces.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record ApproveReturnRequest(
    @NotEmpty Set<String> lineIds,
    @NotBlank String refundAmount,
    @NotBlank String resolutionCode,
    @NotBlank String reason) {}
