package com.novashop.demoweb.web;

import com.novashop.demoweb.infrastructure.client.CommerceClient;
import com.novashop.demoweb.infrastructure.client.CommerceClient.OrderDto;
import com.novashop.demoweb.infrastructure.client.OrderReturnClient;
import com.novashop.demoweb.infrastructure.client.OrderReturnClient.ApproveReturnDto;
import com.novashop.demoweb.infrastructure.client.OrderReturnClient.ApproveResponseDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Agent console. Return creation reads commerce order JSON then POSTs seed fields to order-return
 * (keeps order-return independent if commerce is down after snapshot). Cross-BC HTTP ACL in
 * order-return is deferred.
 */
@Controller
public class AgentReturnController {

  private final OrderReturnClient orderReturnClient;
  private final CommerceClient commerceClient;

  public AgentReturnController(
      OrderReturnClient orderReturnClient, CommerceClient commerceClient) {
    this.orderReturnClient = orderReturnClient;
    this.commerceClient = commerceClient;
  }

  @GetMapping("/agent/returns")
  public String pendingReturns(Model model) {
    model.addAttribute("returns", orderReturnClient.listPending());
    return "agent-returns";
  }

  @PostMapping("/agent/returns/from-order")
  public String requestReturnForOrder(
      @RequestParam String orderId, RedirectAttributes redirectAttributes) {
    OrderDto order = commerceClient.getOrder(orderId);
    String returnId = "ret-" + UUID.randomUUID();
    orderReturnClient.createReturn(ReturnSeedMapper.fromOrder(returnId, order));
    redirectAttributes.addFlashAttribute(
        "message", "Return " + returnId + " created for order " + orderId);
    return "redirect:/agent/returns";
  }

  @PostMapping("/agent/returns/{returnId}/approve")
  public String approve(
      @PathVariable String returnId,
      @RequestParam String actorId,
      @RequestParam String actorRole,
      @RequestParam String refundAmount,
      @RequestParam String reason,
      @RequestParam(defaultValue = "FULL") String resolutionCode,
      @RequestParam(required = false) List<String> lineIds,
      RedirectAttributes redirectAttributes) {
    var rr = orderReturnClient.getReturn(returnId);
    Set<String> lines =
        lineIds == null || lineIds.isEmpty()
            ? rr.lines().stream()
                .map(OrderReturnClient.ReturnLineViewDto::orderLineId)
                .collect(Collectors.toSet())
            : Set.copyOf(lineIds);
    ApproveResponseDto result =
        orderReturnClient.approve(
            returnId,
            new ApproveReturnDto(lines, refundAmount, resolutionCode, reason),
            actorId,
            actorRole);
    return "redirect:/agent/refunds/" + result.refundId();
  }
}
