package com.novashop.paymentrefund.infrastructure.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls unpublished outbox rows and publishes {@code RefundTerminal} JSON to RabbitMQ.
 */
@Component
public class OutboxPublisherJob {

  private static final Logger log = LoggerFactory.getLogger(OutboxPublisherJob.class);

  private final SpringDataOutboxJpaRepository repository;
  private final RabbitTemplate rabbitTemplate;
  private final String exchange;
  private final String routingKey;

  public OutboxPublisherJob(
      SpringDataOutboxJpaRepository repository,
      RabbitTemplate rabbitTemplate,
      @Value("${novashop.messaging.refund-terminal-exchange}") String exchange,
      @Value("${novashop.messaging.refund-terminal-routing-key}") String routingKey) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate, "rabbitTemplate");
    this.exchange = Objects.requireNonNull(exchange, "exchange");
    this.routingKey = Objects.requireNonNull(routingKey, "routingKey");
  }

  @Scheduled(fixedDelayString = "${novashop.outbox.poll-ms:5000}")
  @Transactional
  public void publishPending() {
    List<OutboxEntity> pending = repository.findByPublishedAtIsNullOrderByCreatedAtAsc();
    Instant now = Instant.now();
    for (OutboxEntity row : pending) {
      rabbitTemplate.convertAndSend(exchange, routingKey, row.getPayload());
      log.info(
          "outbox published type={} id={} exchange={} routingKey={}",
          row.getEventType(),
          row.getId(),
          exchange,
          routingKey);
      row.markPublished(now);
    }
  }
}
