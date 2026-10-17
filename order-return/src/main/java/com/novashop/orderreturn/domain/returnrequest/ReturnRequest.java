package com.novashop.orderreturn.domain.returnrequest;

import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ReturnRequest {

  private final String id;
  private final String paymentId;
  private final Money refundableTotal;
  private final List<ReturnLine> lines;
  private final Set<String> refundedLineIds;
  private ReturnStatus status;
  private String refundId;
  private String refundStatus;
  private String actorId;
  private ActorRole actorRole;
  private String reason;
  private String resolutionCode;
  private Money refundAmount;

  private ReturnRequest(
      String id,
      String paymentId,
      Money refundableTotal,
      List<ReturnLine> lines,
      Set<String> refundedLineIds,
      ReturnStatus status) {
    this.id = Objects.requireNonNull(id, "id");
    this.paymentId = Objects.requireNonNull(paymentId, "paymentId");
    this.refundableTotal = Objects.requireNonNull(refundableTotal, "refundableTotal");
    this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    if (this.lines.isEmpty()) {
      throw new IllegalArgumentException("lines");
    }
    this.refundedLineIds = new HashSet<>(Objects.requireNonNull(refundedLineIds, "refundedLineIds"));
    this.status = Objects.requireNonNull(status, "status");
  }

  public static ReturnRequest pending(
      String id,
      String paymentId,
      Money refundableTotal,
      List<ReturnLine> lines,
      Set<String> alreadyApprovedLineIds) {
    return new ReturnRequest(
        id, paymentId, refundableTotal, lines, alreadyApprovedLineIds, ReturnStatus.PENDING_REVIEW);
  }

  /** Rehydrate from persistence — does not enforce pending-only invariants. */
  public static ReturnRequest reconstitute(
      String id,
      String paymentId,
      Money refundableTotal,
      List<ReturnLine> lines,
      Set<String> refundedLineIds,
      ReturnStatus status,
      String refundId,
      String refundStatus,
      String actorId,
      ActorRole actorRole,
      String reason,
      String resolutionCode,
      Money refundAmount) {
    ReturnRequest rr =
        new ReturnRequest(id, paymentId, refundableTotal, lines, refundedLineIds, status);
    rr.refundId = refundId;
    rr.refundStatus = refundStatus;
    rr.actorId = actorId;
    rr.actorRole = actorRole;
    rr.reason = reason;
    rr.resolutionCode = resolutionCode;
    rr.refundAmount = refundAmount;
    return rr;
  }

  public ApprovalDecision approve(ApproveCommand cmd) {
    Objects.requireNonNull(cmd, "cmd");
    if (status != ReturnStatus.PENDING_REVIEW) {
      throw new ReturnRequestException("status must be PENDING_REVIEW");
    }
    validateLines(cmd.lineIds());
    if (cmd.refundAmount().greaterThan(refundableTotal)) {
      throw new ReturnRequestException("refund amount exceeds refundable snapshot");
    }
    if (cmd.role() == ActorRole.AGENT && cmd.refundAmount().greaterThan(cmd.agentLimit())) {
      throw new ReturnRequestException("AGENT may not approve above agent limit");
    }

    String mintedRefundId = UUID.randomUUID().toString();
    this.status = ReturnStatus.APPROVED;
    this.refundId = mintedRefundId;
    this.actorId = cmd.actorId();
    this.actorRole = cmd.role();
    this.reason = cmd.reason();
    this.resolutionCode = cmd.resolutionCode();
    this.refundAmount = cmd.refundAmount();
    this.refundedLineIds.addAll(cmd.lineIds());

    return new ApprovalDecision(mintedRefundId, this.status);
  }

  /**
   * Projects a terminal refund outcome from payment-refund ({@code SUCCEEDED} / {@code FAILED}).
   * Idempotent when the same status is applied again.
   */
  public void applyRefundTerminal(String status) {
    Objects.requireNonNull(status, "status");
    if (this.status != ReturnStatus.APPROVED) {
      throw new ReturnRequestException("refund terminal requires APPROVED return");
    }
    if (this.refundStatus != null && this.refundStatus.equals(status)) {
      return;
    }
    this.refundStatus = status;
  }

  private void validateLines(Set<String> lineIds) {
    Set<String> available =
        lines.stream().map(ReturnLine::orderLineId).collect(Collectors.toUnmodifiableSet());
    for (String lineId : lineIds) {
      if (!available.contains(lineId)) {
        throw new ReturnRequestException("unknown orderLineId: " + lineId);
      }
      if (refundedLineIds.contains(lineId)) {
        throw new ReturnRequestException("orderLineId already approved: " + lineId);
      }
    }
    for (ReturnLine line : lines) {
      if (lineIds.contains(line.orderLineId()) && line.remainingRefundableQty() <= 0) {
        throw new ReturnRequestException(
            "remaining refundable qty exceeded for line: " + line.orderLineId());
      }
    }
  }

  public String id() {
    return id;
  }

  public String paymentId() {
    return paymentId;
  }

  public Money refundableTotal() {
    return refundableTotal;
  }

  public List<ReturnLine> lines() {
    return lines;
  }

  public Set<String> refundedLineIds() {
    return Set.copyOf(refundedLineIds);
  }

  public ReturnStatus status() {
    return status;
  }

  public String refundId() {
    return refundId;
  }

  public String refundStatus() {
    return refundStatus;
  }

  public String actorId() {
    return actorId;
  }

  public ActorRole actorRole() {
    return actorRole;
  }

  public String reason() {
    return reason;
  }

  public String resolutionCode() {
    return resolutionCode;
  }

  public Money refundAmount() {
    return refundAmount;
  }
}
