package com.novashop.paymentrefund.interfaces.api;

import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.RecordRefundResultService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/psp/callbacks")
public class PspCallbackController {

  private final RecordRefundResultService recordRefundResultService;

  public PspCallbackController(RecordRefundResultService recordRefundResultService) {
    this.recordRefundResultService = recordRefundResultService;
  }

  @PostMapping("/refunds")
  public ResponseEntity<Void> refundCallback(@Valid @RequestBody PspRefundCallbackRequest body) {
    recordRefundResultService.handle(body.refundId(), body.success(), body.failureReason());
    return ResponseEntity.accepted().build();
  }
}
