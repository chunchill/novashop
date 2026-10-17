package com.novashop.orderreturn.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares exchanges/queues used by order-return (idempotent AMQP Declarables).
 *
 * <ul>
 *   <li>publishes {@code ReturnApproved} → {@code novashop.return.approved}
 *   <li>consumes {@code RefundTerminal} from {@code order-return.refund-terminal}
 * </ul>
 */
@Configuration
public class RabbitMessagingConfig {

  @Bean
  TopicExchange returnApprovedExchange(
      @Value("${novashop.messaging.return-approved-exchange}") String name) {
    return new TopicExchange(name, true, false);
  }

  @Bean
  TopicExchange refundTerminalExchange(
      @Value("${novashop.messaging.refund-terminal-exchange}") String name) {
    return new TopicExchange(name, true, false);
  }

  @Bean
  Queue refundTerminalQueue(
      @Value("${novashop.messaging.refund-terminal-queue}") String name) {
    return new Queue(name, true);
  }

  @Bean
  Binding refundTerminalBinding(
      Queue refundTerminalQueue,
      TopicExchange refundTerminalExchange,
      @Value("${novashop.messaging.refund-terminal-routing-key}") String routingKey) {
    return BindingBuilder.bind(refundTerminalQueue).to(refundTerminalExchange).with(routingKey);
  }
}
