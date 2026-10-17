package com.novashop.paymentrefund.integration.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.paymentrefund.domain.money.Money;
import com.novashop.paymentrefund.domain.refund.ExternalExecutionStatus;
import com.novashop.paymentrefund.domain.refund.Refund;
import com.novashop.paymentrefund.domain.refund.RefundStatus;
import com.novashop.paymentrefund.infrastructure.outbox.OutboxEntity;
import com.novashop.paymentrefund.infrastructure.outbox.SpringDataOutboxJpaRepository;
import com.novashop.paymentrefund.infrastructure.persistence.JpaRefundRepository;
import com.novashop.paymentrefund.infrastructure.persistence.SpringDataRefundJpaRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * L3 persistence slice test.
 *
 * <p>Uses H2 (MODE=PostgreSQL) because Testcontainers Docker may be unavailable to the Gradle JVM.
 * Prefer Testcontainers postgres when Docker is available to the test process.
 */
@DataJpaTest
@Import(JpaRefundRepository.class)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:payment_refund;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=update",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
    })
class JpaRefundRepositoryIT {

  @Autowired JpaRefundRepository repository;
  @Autowired SpringDataRefundJpaRepository jpa;
  @Autowired SpringDataOutboxJpaRepository outboxJpa;

  @Test
  void persist_and_reload_round_trip_including_execution_state() {
    Refund pending =
        Refund.createPending("rf-it-1", "ret-1", "pay-1", Money.cny("100.00"), "rf-it-1");
    repository.save(pending);

    Refund loaded = repository.findById("rf-it-1").orElseThrow();
    assertThat(loaded.status()).isEqualTo(RefundStatus.PENDING_EXTERNAL_EXECUTION);
    assertThat(loaded.latestExecution().attemptCount()).isZero();
    assertThat(loaded.latestExecution().status()).isEqualTo(ExternalExecutionStatus.PENDING);

    loaded.recordExecutionAttempt();
    loaded.recordResult(true, null);
    repository.save(loaded);

    outboxJpa.save(
        new OutboxEntity(
            "obx-1",
            "com.novashop.platform.events.RefundTerminal",
            "{\"refundId\":\"rf-it-1\"}",
            Instant.parse("2026-01-01T00:00:00Z")));

    Refund reloaded = repository.findById("rf-it-1").orElseThrow();
    assertThat(reloaded.status()).isEqualTo(RefundStatus.SUCCEEDED);
    assertThat(reloaded.latestExecution().attemptCount()).isEqualTo(1);
    assertThat(reloaded.latestExecution().status()).isEqualTo(ExternalExecutionStatus.SUCCEEDED);
    assertThat(jpa.count()).isEqualTo(1);
    assertThat(outboxJpa.count()).isEqualTo(1);
  }
}
