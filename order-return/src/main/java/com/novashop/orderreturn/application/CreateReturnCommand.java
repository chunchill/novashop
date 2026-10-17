package com.novashop.orderreturn.application;

import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record CreateReturnCommand(
    String id,
    String paymentId,
    Money refundableTotal,
    List<ReturnLine> lines,
    Set<String> alreadyApprovedLineIds) {

  public CreateReturnCommand {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(paymentId, "paymentId");
    Objects.requireNonNull(refundableTotal, "refundableTotal");
    lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    alreadyApprovedLineIds =
        Set.copyOf(Objects.requireNonNull(alreadyApprovedLineIds, "alreadyApprovedLineIds"));
  }
}
