package com.novashop.paymentrefund.infrastructure.persistence;

import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.domain.refund.ExternalExecution;
import com.novashop.paymentrefund.domain.refund.ExternalExecutionStatus;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.domain.refund.RefundStatus;

final class RefundMapper {

  private RefundMapper() {}

  static RefundEntity toEntity(Refund domain) {
    RefundEntity entity = new RefundEntity();
    entity.setId(domain.id());
    copyToEntity(domain, entity);
    return entity;
  }

  static void copyToEntity(Refund domain, RefundEntity entity) {
    entity.setReturnRequestId(domain.returnRequestId());
    entity.setPaymentId(domain.paymentId());
    entity.setAmount(domain.amount().amount());
    entity.setCurrency(domain.amount().currency());
    entity.setIdempotencyKey(domain.idempotencyKey());
    entity.setStatus(domain.status().name());

    ExternalExecution execution = domain.latestExecution();
    ExternalExecutionEntity executionEntity = entity.getLatestExecution();
    if (executionEntity == null) {
      executionEntity = new ExternalExecutionEntity();
      // Stable id derived from refund id so orphanRemoval + OneToOne stay simple.
      executionEntity.setId(domain.id() + "-exec");
      entity.setLatestExecution(executionEntity);
    }
    executionEntity.setIdempotencyKey(execution.idempotencyKey());
    executionEntity.setAttemptCount(execution.attemptCount());
    executionEntity.setStatus(execution.status().name());
    executionEntity.setLastError(execution.lastError());
  }

  static Refund toDomain(RefundEntity entity) {
    ExternalExecutionEntity executionEntity = entity.getLatestExecution();
    ExternalExecution execution =
        ExternalExecution.reconstitute(
            executionEntity.getIdempotencyKey(),
            executionEntity.getAttemptCount(),
            ExternalExecutionStatus.valueOf(executionEntity.getStatus()),
            executionEntity.getLastError());
    return Refund.reconstitute(
        entity.getId(),
        entity.getReturnRequestId(),
        entity.getPaymentId(),
        Money.of(entity.getAmount(), entity.getCurrency()),
        entity.getIdempotencyKey(),
        RefundStatus.valueOf(entity.getStatus()),
        execution);
  }
}
