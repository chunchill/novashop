package com.novashop.paymentrefund.unit.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.novashop.paymentrefund.infrastructure.outbox.OutboxEntity;
import com.novashop.paymentrefund.infrastructure.outbox.OutboxPublisherJob;
import com.novashop.paymentrefund.infrastructure.outbox.SpringDataOutboxJpaRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class OutboxPublisherJobTest {

  @Test
  void publishes_refund_terminal_then_marks_published() {
    SpringDataOutboxJpaRepository repository = mock(SpringDataOutboxJpaRepository.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    OutboxEntity row =
        new OutboxEntity(
            "obx-1",
            "com.novashop.platform.events.RefundTerminal",
            "{\"refundId\":\"rf-1\",\"status\":\"SUCCEEDED\"}",
            Instant.parse("2026-01-01T00:00:00Z"));
    when(repository.findByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(row));

    var job =
        new OutboxPublisherJob(
            repository, rabbitTemplate, "novashop.refund.terminal", "refund.terminal");
    job.publishPending();

    verify(rabbitTemplate)
        .convertAndSend(
            eq("novashop.refund.terminal"),
            eq("refund.terminal"),
            eq("{\"refundId\":\"rf-1\",\"status\":\"SUCCEEDED\"}"));
    assertThat(row.isPublished()).isTrue();
  }
}
