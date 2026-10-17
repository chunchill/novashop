package com.novashop.paymentrefund.infrastructure.config;

import com.novashop.paymentrefund.application.ExecuteRefundHandler;
import com.novashop.paymentrefund.application.RecordRefundResultHandler;
import com.novashop.paymentrefund.application.RetryExternalExecutionHandler;
import com.novashop.paymentrefund.application.StartRefundFromReturnApprovedHandler;
import com.novashop.paymentrefund.application.port.PspRefundClient;
import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.application.port.RefundTerminalOutboxPort;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.platform.events.ReturnApproved;
import java.util.Objects;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ApplicationHandlersConfig {

  @Bean
  StartRefundFromReturnApprovedHandler startRefundFromReturnApprovedHandler(
      RefundRepository repository) {
    return new StartRefundFromReturnApprovedHandler(repository);
  }

  @Bean
  ExecuteRefundHandler executeRefundHandler(
      RefundRepository repository, PspRefundClient pspClient, RefundTerminalOutboxPort outbox) {
    return new ExecuteRefundHandler(repository, pspClient, outbox);
  }

  @Bean
  RecordRefundResultHandler recordRefundResultHandler(
      RefundRepository repository, RefundTerminalOutboxPort outbox) {
    return new RecordRefundResultHandler(repository, outbox);
  }

  @Bean
  RetryExternalExecutionHandler retryExternalExecutionHandler(
      RefundRepository repository, ExecuteRefundHandler executeRefundHandler) {
    return new RetryExternalExecutionHandler(repository, executeRefundHandler);
  }

  @Service
  public static class StartRefundService {

    private final StartRefundFromReturnApprovedHandler handler;

    public StartRefundService(StartRefundFromReturnApprovedHandler handler) {
      this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Transactional
    public void handle(ReturnApproved event) {
      handler.handle(event);
    }
  }

  @Service
  public static class ExecuteRefundService {

    private final ExecuteRefundHandler handler;

    public ExecuteRefundService(ExecuteRefundHandler handler) {
      this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Transactional
    public void handle(String refundId) {
      handler.handle(refundId);
    }
  }

  @Service
  public static class RecordRefundResultService {

    private final RecordRefundResultHandler handler;

    public RecordRefundResultService(RecordRefundResultHandler handler) {
      this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Transactional
    public void handle(String refundId, boolean success, String failureReason) {
      handler.handle(refundId, success, failureReason);
    }
  }

  @Service
  public static class RetryExternalExecutionService {

    private final RetryExternalExecutionHandler handler;

    public RetryExternalExecutionService(RetryExternalExecutionHandler handler) {
      this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Transactional
    public void handle(String refundId) {
      handler.handle(refundId);
    }
  }

  @Service
  public static class GetRefundService {

    private final RefundRepository repository;

    public GetRefundService(RefundRepository repository) {
      this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Transactional(readOnly = true)
    public Optional<Refund> handle(String id) {
      return repository.findById(id);
    }
  }
}
