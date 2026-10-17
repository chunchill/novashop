package com.novashop.orderreturn.infrastructure.persistence;

import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class ReturnRequestMapper {

  private ReturnRequestMapper() {}

  static ReturnRequestEntity toEntity(ReturnRequest domain) {
    ReturnRequestEntity entity = new ReturnRequestEntity();
    entity.setId(domain.id());
    entity.setPaymentId(domain.paymentId());
    entity.setRefundableAmount(domain.refundableTotal().amount());
    entity.setRefundableCurrency(domain.refundableTotal().currency());
    entity.setStatus(domain.status().name());
    entity.setRefundId(domain.refundId());
    entity.setRefundStatus(domain.refundStatus());
    entity.setActorId(domain.actorId());
    entity.setActorRole(domain.actorRole() == null ? null : domain.actorRole().name());
    entity.setReason(domain.reason());
    entity.setResolutionCode(domain.resolutionCode());
    if (domain.refundAmount() != null) {
      entity.setRefundAmount(domain.refundAmount().amount());
      entity.setRefundCurrency(domain.refundAmount().currency());
    }
    entity.setLines(
        domain.lines().stream()
            .map(l -> new ReturnLineEntity(l.orderLineId(), l.remainingRefundableQty()))
            .collect(Collectors.toCollection(java.util.ArrayList::new)));
    entity.setRefundedLineIds(new java.util.HashSet<>(domain.refundedLineIds()));
    return entity;
  }

  static void copyToEntity(ReturnRequest domain, ReturnRequestEntity entity) {
    entity.setPaymentId(domain.paymentId());
    entity.setRefundableAmount(domain.refundableTotal().amount());
    entity.setRefundableCurrency(domain.refundableTotal().currency());
    entity.setStatus(domain.status().name());
    entity.setRefundId(domain.refundId());
    entity.setRefundStatus(domain.refundStatus());
    entity.setActorId(domain.actorId());
    entity.setActorRole(domain.actorRole() == null ? null : domain.actorRole().name());
    entity.setReason(domain.reason());
    entity.setResolutionCode(domain.resolutionCode());
    if (domain.refundAmount() != null) {
      entity.setRefundAmount(domain.refundAmount().amount());
      entity.setRefundCurrency(domain.refundAmount().currency());
    } else {
      entity.setRefundAmount(null);
      entity.setRefundCurrency(null);
    }
    entity.getLines().clear();
    domain
        .lines()
        .forEach(
            l ->
                entity
                    .getLines()
                    .add(new ReturnLineEntity(l.orderLineId(), l.remainingRefundableQty())));
    entity.getRefundedLineIds().clear();
    entity.getRefundedLineIds().addAll(domain.refundedLineIds());
  }

  static ReturnRequest toDomain(ReturnRequestEntity entity) {
    List<ReturnLine> lines =
        entity.getLines().stream()
            .map(l -> new ReturnLine(l.getOrderLineId(), l.getRemainingRefundableQty()))
            .toList();
    Set<String> refunded = Set.copyOf(entity.getRefundedLineIds());
    Money refundAmount =
        entity.getRefundAmount() == null
            ? null
            : Money.of(entity.getRefundAmount(), entity.getRefundCurrency());
    ActorRole role =
        entity.getActorRole() == null ? null : ActorRole.parse(entity.getActorRole());
    return ReturnRequest.reconstitute(
        entity.getId(),
        entity.getPaymentId(),
        Money.of(entity.getRefundableAmount(), entity.getRefundableCurrency()),
        lines,
        refunded,
        ReturnStatus.valueOf(entity.getStatus()),
        entity.getRefundId(),
        entity.getRefundStatus(),
        entity.getActorId(),
        role,
        entity.getReason(),
        entity.getResolutionCode(),
        refundAmount);
  }
}
