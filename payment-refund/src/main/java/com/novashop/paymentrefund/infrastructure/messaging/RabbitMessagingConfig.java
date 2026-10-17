package com.novashop.paymentrefund.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares exchanges/queues used by payment-refund (idempotent AMQP Declarables).
 *
 * <ul>
 *   <li>consumes {@code ReturnApproved} from {@code payment-refund.return-approved}
 *   <li>publishes {@code RefundTerminal} → {@code novashop.refund.terminal}
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
  Queue returnApprovedQueue(
      @Value("${novashop.messaging.return-approved-queue}") String name) {
    return new Queue(name, true);
  }

  @Bean
  Binding returnApprovedBinding(
      Queue returnApprovedQueue,
      TopicExchange returnApprovedExchange,
      @Value("${novashop.messaging.return-approved-routing-key}") String routingKey) {
    return BindingBuilder.bind(returnApprovedQueue).to(returnApprovedExchange).with(routingKey);
  }

  @Bean
  TopicExchange refundTerminalExchange(
      @Value("${novashop.messaging.refund-terminal-exchange}") String name) {
    return new TopicExchange(name, true, false);
  }
}
