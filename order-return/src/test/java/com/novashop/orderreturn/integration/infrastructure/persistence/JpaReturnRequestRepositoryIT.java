package com.novashop.orderreturn.integration.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ApproveCommand;
import com.novashop.orderreturn.domain.returnrequest.ReturnLine;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import com.novashop.orderreturn.infrastructure.outbox.OutboxEntity;
import com.novashop.orderreturn.infrastructure.outbox.SpringDataOutboxJpaRepository;
import com.novashop.orderreturn.infrastructure.persistence.JpaReturnRequestRepository;
import com.novashop.orderreturn.infrastructure.persistence.SpringDataReturnRequestJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * L3 persistence slice test.
 *
 * <p>Uses H2 (MODE=PostgreSQL) because Testcontainers could not locate a Docker environment in
 * this agent's Gradle JVM (Docker CLI works; DockerClientProviderStrategy failed). Prefer
 * Testcontainers postgres when Docker is available to the test process.
 */
@DataJpaTest
@Import(JpaReturnRequestRepository.class)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:order_return;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=update",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
    })
class JpaReturnRequestRepositoryIT {

  @Autowired JpaReturnRequestRepository repository;
  @Autowired SpringDataReturnRequestJpaRepository jpa;
  @Autowired SpringDataOutboxJpaRepository outboxJpa;

  @Test
  void persist_and_reload_round_trip_including_approved_state() {
    ReturnRequest pending =
        ReturnRequest.pending(
            "ret-it-1",
            "pay-1",
            Money.cny("200.00"),
            List.of(new ReturnLine("L1", 1)),
            Set.of());
    repository.save(pending);

    ReturnRequest loaded = repository.findById("ret-it-1").orElseThrow();
    assertThat(loaded.status()).isEqualTo(ReturnStatus.PENDING_REVIEW);
    assertThat(loaded.lines()).hasSize(1);
    assertThat(loaded.refundableTotal()).isEqualTo(Money.cny("200.00"));

    loaded.approve(
        new ApproveCommand(
            Set.of("L1"),
            Money.cny("200.00"),
            "FULL",
            "damaged",
            "agent-1",
            ActorRole.AGENT,
            Money.cny("500.00")));
    repository.save(loaded);

    outboxJpa.save(
        new OutboxEntity(
            "obx-1",
            "com.novashop.platform.events.ReturnApproved",
            "{\"refundId\":\"x\"}",
            Instant.parse("2026-01-01T00:00:00Z")));

    ReturnRequest reloaded = repository.findById("ret-it-1").orElseThrow();
    assertThat(reloaded.status()).isEqualTo(ReturnStatus.APPROVED);
    assertThat(reloaded.refundId()).isNotBlank();
    assertThat(reloaded.refundedLineIds()).containsExactly("L1");
    assertThat(reloaded.actorRole()).isEqualTo(ActorRole.AGENT);
    assertThat(repository.findByStatus(ReturnStatus.PENDING_REVIEW)).isEmpty();
    assertThat(jpa.count()).isEqualTo(1);
    assertThat(outboxJpa.count()).isEqualTo(1);
  }
}
