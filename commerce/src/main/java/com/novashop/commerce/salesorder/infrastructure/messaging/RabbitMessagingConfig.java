package com.novashop.commerce.salesorder.infrastructure.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the {@code OrderPlaced} exchange (idempotent AMQP Declarable).
 *
 * <p>Phase 3 fulfillment will bind a queue to consume from this exchange.
 */
@Configuration
public class RabbitMessagingConfig {

  @Bean
  TopicExchange orderPlacedExchange(
      @Value("${novashop.messaging.order-placed-exchange}") String name) {
    return new TopicExchange(name, true, false);
  }
}
