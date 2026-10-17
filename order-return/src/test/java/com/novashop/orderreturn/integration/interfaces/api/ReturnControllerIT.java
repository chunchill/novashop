package com.novashop.orderreturn.integration.interfaces.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.ApproveReturnService;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.CreateReturnService;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.GetReturnService;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.ListPendingReturnsService;
import com.novashop.orderreturn.interfaces.api.ActorHeaders;
import com.novashop.orderreturn.interfaces.api.ReturnController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ReturnController.class)
@ActiveProfiles("test")
class ReturnControllerIT {

  @Autowired MockMvc mockMvc;

  @MockBean CreateReturnService createReturnService;
  @MockBean ListPendingReturnsService listPendingReturnsService;
  @MockBean ApproveReturnService approveReturnService;
  @MockBean GetReturnService getReturnService;

  @Test
  void approve_missing_role_header_returns_400() throws Exception {
    mockMvc
        .perform(
            post("/returns/ret-1/approve")
                .header(ActorHeaders.ACTOR_ID, "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "lineIds": ["L1"],
                      "refundAmount": "100.00",
                      "resolutionCode": "FULL",
                      "reason": "damaged"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.header").value(ActorHeaders.ACTOR_ROLE));
  }
}
