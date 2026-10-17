package com.novashop.paymentrefund.interfaces.api;

import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.GetRefundService;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.RetryExternalExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refunds")
public class RefundController {

  private final GetRefundService getRefundService;
  private final RetryExternalExecutionService retryExternalExecutionService;

  public RefundController(
      GetRefundService getRefundService,
      RetryExternalExecutionService retryExternalExecutionService) {
    this.getRefundService = getRefundService;
    this.retryExternalExecutionService = retryExternalExecutionService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<RefundResponse> get(@PathVariable String id) {
    return getRefundService
        .handle(id)
        .map(RefundResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/retry")
  public ResponseEntity<Void> retry(@PathVariable String id) {
    retryExternalExecutionService.handle(id);
    return ResponseEntity.accepted().build();
  }
}
