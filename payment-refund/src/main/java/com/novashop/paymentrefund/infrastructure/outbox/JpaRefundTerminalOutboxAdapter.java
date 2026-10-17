package com.novashop.paymentrefund.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novashop.paymentrefund.application.port.RefundTerminalOutboxPort;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaRefundTerminalOutboxAdapter implements RefundTerminalOutboxPort {

  private final SpringDataOutboxJpaRepository repository;
  private final ObjectMapper objectMapper;

  public JpaRefundTerminalOutboxAdapter(
      SpringDataOutboxJpaRepository repository, ObjectMapper objectMapper) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
  }

  @Override
  public void append(Object event) {
    Objects.requireNonNull(event, "event");
    String payload;
    try {
      payload = objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("failed to serialize outbox event", e);
    }
    OutboxEntity row =
        new OutboxEntity(
            UUID.randomUUID().toString(),
            event.getClass().getName(),
            payload,
            Instant.now());
    repository.save(row);
  }
}
