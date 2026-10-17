package com.novashop.orderreturn.unit.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.ApplyRefundTerminalService;
import com.novashop.orderreturn.infrastructure.messaging.RefundTerminalListener;
import com.novashop.platform.events.RefundTerminal;
import org.junit.jupiter.api.Test;

class RefundTerminalListenerTest {

  @Test
  void deserializes_and_delegates_to_application_service() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ApplyRefundTerminalService service = mock(ApplyRefundTerminalService.class);
    var listener = new RefundTerminalListener(objectMapper, service);

    String payload =
        """
        {"refundId":"rf-1","returnRequestId":"ret-1","status":"SUCCEEDED","failureReason":null}
        """;
    listener.onMessage(payload);

    verify(service)
        .handle(
            assertArg(
                (RefundTerminal event) -> {
                  org.assertj.core.api.Assertions.assertThat(event.refundId()).isEqualTo("rf-1");
                  org.assertj.core.api.Assertions.assertThat(event.returnRequestId())
                      .isEqualTo("ret-1");
                  org.assertj.core.api.Assertions.assertThat(event.status()).isEqualTo("SUCCEEDED");
                }));
  }
}
