package com.novashop.paymentrefund.unit.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.ExecuteRefundService;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.StartRefundService;
import com.novashop.paymentrefund.infrastructure.messaging.ReturnApprovedListener;
import com.novashop.platform.events.ReturnApproved;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReturnApprovedListenerMappingTest {

  @Test
  void deserializes_and_invokes_handlers_in_order() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    StartRefundService start = mock(StartRefundService.class);
    ExecuteRefundService execute = mock(ExecuteRefundService.class);
    var listener = new ReturnApprovedListener(objectMapper, start, execute);

    ReturnApproved event =
        new ReturnApproved(
            "rf-1",
            "ret-1",
            "pay-1",
            new BigDecimal("100.00"),
            "CNY",
            List.of("L1"),
            "agent-1",
            "AGENT",
            "damaged",
            "FULL",
            "rf-1");
    String payload = objectMapper.writeValueAsString(event);

    listener.onMessage(payload);

    verify(start)
        .handle(
            assertArg(
                (ReturnApproved e) -> {
                  org.assertj.core.api.Assertions.assertThat(e.refundId()).isEqualTo("rf-1");
                  org.assertj.core.api.Assertions.assertThat(e.returnRequestId()).isEqualTo("ret-1");
                }));
    verify(execute).handle(eq("rf-1"));
  }
}
