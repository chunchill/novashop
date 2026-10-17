package com.novashop.orderreturn.application;

import com.novashop.orderreturn.application.port.OutboxPort;
import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ApprovalDecision;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.platform.events.ReturnApproved;
import java.util.ArrayList;
import java.util.Objects;

public class ApproveReturnHandler {

  private final ReturnRequestRepository repository;
  private final OutboxPort outbox;
  private final Money agentLimit;

  public ApproveReturnHandler(
      ReturnRequestRepository repository, OutboxPort outbox, Money agentLimit) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.outbox = Objects.requireNonNull(outbox, "outbox");
    this.agentLimit = Objects.requireNonNull(agentLimit, "agentLimit");
  }

  public ApproveResult handle(
      String returnId, ApproveReturnCommand cmd, String actorId, ActorRole role) {
    Objects.requireNonNull(returnId, "returnId");
    Objects.requireNonNull(cmd, "cmd");
    Objects.requireNonNull(actorId, "actorId");
    Objects.requireNonNull(role, "role");

    ReturnRequest rr =
        repository
            .findById(returnId)
            .orElseThrow(() -> new IllegalArgumentException("return not found: " + returnId));

    ApprovalDecision decision = rr.approve(cmd.toDomain(actorId, role, agentLimit));
    repository.save(rr);
    outbox.append(toEvent(rr, decision, cmd));
    return new ApproveResult(returnId, decision.refundId());
  }

  private static ReturnApproved toEvent(
      ReturnRequest rr, ApprovalDecision decision, ApproveReturnCommand cmd) {
    return new ReturnApproved(
        decision.refundId(),
        rr.id(),
        rr.paymentId(),
        cmd.refundAmount().amount(),
        cmd.refundAmount().currency(),
        new ArrayList<>(cmd.lineIds()),
        rr.actorId(),
        rr.actorRole().name(),
        cmd.reason(),
        cmd.resolutionCode(),
        decision.refundId());
  }
}
