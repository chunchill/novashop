package com.novashop.paymentrefund.integration.interfaces.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.GetRefundService;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.RecordRefundResultService;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.RetryExternalExecutionService;
import com.novashop.paymentrefund.interfaces.api.PspCallbackController;
import com.novashop.paymentrefund.interfaces.api.RefundController;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {RefundController.class, PspCallbackController.class})
@ActiveProfiles("test")
class RefundControllerIT {

  @Autowired MockMvc mockMvc;

  @MockBean GetRefundService getRefundService;
  @MockBean RetryExternalExecutionService retryExternalExecutionService;
  @MockBean RecordRefundResultService recordRefundResultService;

  @Test
  void get_refund_includes_latest_execution_fields() throws Exception {
    Refund refund =
        Refund.createPending("rf-1", "ret-1", "pay-1", Money.cny("50.00"), "rf-1");
    when(getRefundService.handle("rf-1")).thenReturn(Optional.of(refund));

    mockMvc
        .perform(get("/refunds/rf-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("rf-1"))
        .andExpect(jsonPath("$.status").value("PENDING_EXTERNAL_EXECUTION"))
        .andExpect(jsonPath("$.latestExecution.attemptCount").value(0))
        .andExpect(jsonPath("$.latestExecution.status").value("PENDING"));
  }

  @Test
  void get_missing_refund_returns_404() throws Exception {
    when(getRefundService.handle("missing")).thenReturn(Optional.empty());

    mockMvc.perform(get("/refunds/missing")).andExpect(status().isNotFound());
  }

  @Test
  void psp_callback_accepts_valid_body() throws Exception {
    mockMvc
        .perform(
            post("/psp/callbacks/refunds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "refundId": "rf-1",
                      "success": true,
                      "failureReason": null
                    }
                    """))
        .andExpect(status().isAccepted());
  }
}
