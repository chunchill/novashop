package com.novashop.paymentrefund.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.ExecuteRefundService;
import com.novashop.paymentrefund.infrastructure.config.ApplicationHandlersConfig.StartRefundService;
import com.novashop.platform.events.ReturnApproved;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Deserializes {@link ReturnApproved} and invokes application handlers only — never injects {@code
 * PspRefundClient}.
 */
@Component
public class ReturnApprovedListener {

  private static final Logger log = LoggerFactory.getLogger(ReturnApprovedListener.class);

  private final ObjectMapper objectMapper;
  private final StartRefundService startRefundService;
  private final ExecuteRefundService executeRefundService;

  public ReturnApprovedListener(
      ObjectMapper objectMapper,
      StartRefundService startRefundService,
      ExecuteRefundService executeRefundService) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    this.startRefundService = Objects.requireNonNull(startRefundService, "startRefundService");
    this.executeRefundService =
        Objects.requireNonNull(executeRefundService, "executeRefundService");
  }

  @RabbitListener(queues = "${novashop.messaging.return-approved-queue}")
  public void onMessage(String payload) throws Exception {
    ReturnApproved event = objectMapper.readValue(payload, ReturnApproved.class);
    log.info(
        "ReturnApproved received refundId={} returnRequestId={}",
        event.refundId(),
        event.returnRequestId());
    startRefundService.handle(event);
    executeRefundService.handle(event.refundId());
  }
}
