package com.novashop.orderreturn.interfaces.api;

import com.novashop.orderreturn.application.ApproveReturnCommand;
import com.novashop.orderreturn.application.CreateReturnCommand;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.ApproveReturnService;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.CreateReturnService;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.GetReturnService;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.ListPendingReturnsService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/returns")
public class ReturnController {

  private final CreateReturnService createReturnService;
  private final ListPendingReturnsService listPendingReturnsService;
  private final ApproveReturnService approveReturnService;
  private final GetReturnService getReturnService;

  public ReturnController(
      CreateReturnService createReturnService,
      ListPendingReturnsService listPendingReturnsService,
      ApproveReturnService approveReturnService,
      GetReturnService getReturnService) {
    this.createReturnService = createReturnService;
    this.listPendingReturnsService = listPendingReturnsService;
    this.approveReturnService = approveReturnService;
    this.getReturnService = getReturnService;
  }

  @PostMapping
  public ResponseEntity<Map<String, String>> create(@Valid @RequestBody CreateReturnRequest body) {
    Set<String> already =
        body.alreadyApprovedLineIds() == null ? Set.of() : body.alreadyApprovedLineIds();
    List<ReturnLine> lines =
        body.lines().stream()
            .map(l -> new ReturnLine(l.orderLineId(), l.remainingRefundableQty()))
            .toList();
    String id =
        createReturnService.handle(
            new CreateReturnCommand(
                body.id(),
                body.paymentId(),
                Money.cny(body.refundableTotal()),
                lines,
                already));
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
  }

  @GetMapping("/pending")
  public List<ReturnResponse> listPending() {
    return listPendingReturnsService.handle().stream().map(ReturnResponse::from).toList();
  }

  @PostMapping("/{id}/approve")
  public ApproveReturnResponse approve(
      @PathVariable String id,
      @Valid @RequestBody ApproveReturnRequest body,
      @RequestHeader(value = ActorHeaders.ACTOR_ID, required = false) String actorId,
      @RequestHeader(value = ActorHeaders.ACTOR_ROLE, required = false) String actorRole) {
    ActorHeaders actor = ActorHeaders.require(actorId, actorRole);
    var result =
        approveReturnService.handle(
            id,
            new ApproveReturnCommand(
                body.lineIds(),
                Money.cny(body.refundAmount()),
                body.resolutionCode(),
                body.reason()),
            actor.actorId(),
            actor.role());
    return ApproveReturnResponse.from(result);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReturnResponse> get(@PathVariable String id) {
    return getReturnService
        .handle(id)
        .map(ReturnResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
