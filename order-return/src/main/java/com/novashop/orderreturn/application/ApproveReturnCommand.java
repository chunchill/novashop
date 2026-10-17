package com.novashop.orderreturn.application;

import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ApproveCommand;
import java.util.Objects;
import java.util.Set;

public record ApproveReturnCommand(
    Set<String> lineIds, Money refundAmount, String resolutionCode, String reason) {

  public ApproveReturnCommand {
    lineIds = Set.copyOf(Objects.requireNonNull(lineIds, "lineIds"));
    Objects.requireNonNull(refundAmount, "refundAmount");
    Objects.requireNonNull(resolutionCode, "resolutionCode");
    Objects.requireNonNull(reason, "reason");
  }

  public ApproveCommand toDomain(String actorId, ActorRole role, Money agentLimit) {
    return new ApproveCommand(
        lineIds, refundAmount, resolutionCode, reason, actorId, role, agentLimit);
  }
}
