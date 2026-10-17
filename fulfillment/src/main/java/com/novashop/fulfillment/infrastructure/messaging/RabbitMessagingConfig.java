package com.novashop.fulfillment.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consumes {@code OrderPlaced} from commerce outbox publish
 * ({@code novashop.order.placed} / {@code order.placed}).
 */
@Configuration
public class RabbitMessagingConfig {

  @Bean
  TopicExchange orderPlacedExchange(
      @Value("${novashop.messaging.order-placed-exchange}") String name) {
    return new TopicExchange(name, true, false);
  }

  @Bean
  Queue orderPlacedQueue(@Value("${novashop.messaging.order-placed-queue}") String name) {
    return new Queue(name, true);
  }

  @Bean
  Binding orderPlacedBinding(
      Queue orderPlacedQueue,
      TopicExchange orderPlacedExchange,
      @Value("${novashop.messaging.order-placed-routing-key}") String routingKey) {
    return BindingBuilder.bind(orderPlacedQueue).to(orderPlacedExchange).with(routingKey);
  }
}
