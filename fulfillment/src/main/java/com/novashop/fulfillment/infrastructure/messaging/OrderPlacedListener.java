package com.novashop.fulfillment.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novashop.fulfillment.infrastructure.config.ApplicationHandlersConfig.ReserveStockService;
import com.novashop.platform.events.OrderPlaced;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Deserializes {@link OrderPlaced} and invokes the reserve-stock use case only — no domain rules
 * here.
 */
@Component
public class OrderPlacedListener {

  private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

  private final ObjectMapper objectMapper;
  private final ReserveStockService reserveStockService;

  public OrderPlacedListener(ObjectMapper objectMapper, ReserveStockService reserveStockService) {
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    this.reserveStockService = Objects.requireNonNull(reserveStockService, "reserveStockService");
  }

  @RabbitListener(queues = "${novashop.messaging.order-placed-queue}")
  public void onMessage(String payload) throws Exception {
    OrderPlaced event = objectMapper.readValue(payload, OrderPlaced.class);
    log.info("OrderPlaced received orderId={}", event.orderId());
    reserveStockService.handle(event);
  }
}
