package com.novashop.orderreturn.domain.returnrequest;

import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import java.util.Objects;
import java.util.Set;

public record ApproveCommand(
    Set<String> lineIds,
    Money refundAmount,
    String resolutionCode,
    String reason,
    String actorId,
    ActorRole role,
    Money agentLimit) {

  public ApproveCommand {
    lineIds = Set.copyOf(Objects.requireNonNull(lineIds, "lineIds"));
    if (lineIds.isEmpty()) {
      throw new IllegalArgumentException("lineIds");
    }
    Objects.requireNonNull(refundAmount, "refundAmount");
    Objects.requireNonNull(resolutionCode, "resolutionCode");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(actorId, "actorId");
    Objects.requireNonNull(role, "role");
    Objects.requireNonNull(agentLimit, "agentLimit");
  }
}
