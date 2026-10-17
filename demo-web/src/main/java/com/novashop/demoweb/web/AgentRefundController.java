package com.novashop.demoweb.web;

import com.novashop.demoweb.infrastructure.client.PaymentRefundClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AgentRefundController {

  private final PaymentRefundClient paymentRefundClient;

  public AgentRefundController(PaymentRefundClient paymentRefundClient) {
    this.paymentRefundClient = paymentRefundClient;
  }

  @GetMapping("/agent/refunds/{refundId}")
  public String refundDetail(@PathVariable String refundId, Model model) {
    model.addAttribute("refund", paymentRefundClient.getRefund(refundId));
    return "agent-refund";
  }

  /**
   * Teaching-only demo: posts a successful PSP callback so learners can leave
   * {@code PENDING_EXTERNAL_EXECUTION} without curling.
   */
  @PostMapping("/agent/refunds/{refundId}/simulate-psp-success")
  public String simulatePspSuccess(
      @PathVariable String refundId, RedirectAttributes redirectAttributes) {
    paymentRefundClient.simulatePspCallbackSuccess(refundId);
    redirectAttributes.addFlashAttribute(
        "message", "Demo: simulated PSP callback success for " + refundId);
    return "redirect:/agent/refunds/" + refundId;
  }
}
