package com.novashop.orderreturn.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novashop.orderreturn.infrastructure.config.ApplicationHandlersConfig.ApplyRefundTerminalService;
import com.novashop.platform.events.RefundTerminal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Deserializes {@link RefundTerminal} and invokes the application handler only.
 */
@Component
public class RefundTerminalListener {

  private static final Logger log = LoggerFactory.getLogger(RefundTerminalListener.class);

  private final ObjectMapper objectMapper;
  private final ApplyRefundTerminalService applyRefundTerminalService;

  public RefundTerminalListener(
      ObjectMapper objectMapper, ApplyRefundTerminalService applyRefundTerminalService) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    this.applyRefundTerminalService =
        Objects.requireNonNull(applyRefundTerminalService, "applyRefundTerminalService");
  }

  @RabbitListener(queues = "${novashop.messaging.refund-terminal-queue}")
  public void onMessage(String payload) throws Exception {
    RefundTerminal event = objectMapper.readValue(payload, RefundTerminal.class);
    log.info(
        "RefundTerminal received refundId={} returnRequestId={} status={}",
        event.refundId(),
        event.returnRequestId(),
        event.status());
    applyRefundTerminalService.handle(event);
  }
}
