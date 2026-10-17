package com.novashop.commerce.integration.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.commerce.salesorder.domain.money.Money;
import com.novashop.commerce.salesorder.domain.order.OrderLine;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import com.novashop.commerce.salesorder.infrastructure.outbox.OutboxEntity;
import com.novashop.commerce.salesorder.infrastructure.outbox.SpringDataOutboxJpaRepository;
import com.novashop.commerce.salesorder.infrastructure.persistence.JpaSalesOrderRepository;
import com.novashop.commerce.salesorder.infrastructure.persistence.SpringDataSalesOrderJpaRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * L3 persistence slice: SalesOrder round-trip plus outbox row presence.
 *
 * <p>Uses H2 (MODE=PostgreSQL) to match order-return IT pattern when Docker is unavailable to the
 * Gradle JVM.
 */
@DataJpaTest
@Import(JpaSalesOrderRepository.class)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:commerce_so;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=update",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
    })
class JpaSalesOrderRepositoryIT {

  @Autowired JpaSalesOrderRepository repository;
  @Autowired SpringDataSalesOrderJpaRepository jpa;
  @Autowired SpringDataOutboxJpaRepository outboxJpa;

  @Test
  void persist_and_reload_sales_order_with_outbox_row() {
    SalesOrder order =
        SalesOrder.reconstitute(
            "ord-it-1",
            "pay-it-1",
            List.of(new OrderLine("L1", "sku-mug-std", "Mug", 2, Money.cny("49.00"))),
            Money.cny("98.00"),
            Money.cny("98.00"));
    repository.save(order);

    outboxJpa.save(
        new OutboxEntity(
            "obx-it-1",
            "com.novashop.platform.events.OrderPlaced",
            "{\"orderId\":\"ord-it-1\"}",
            Instant.parse("2026-01-01T00:00:00Z")));

    SalesOrder loaded = repository.findById("ord-it-1").orElseThrow();
    assertThat(loaded.paymentId()).isEqualTo("pay-it-1");
    assertThat(loaded.lines()).hasSize(1);
    assertThat(loaded.total().amount()).isEqualByComparingTo("98.00");
    assertThat(jpa.count()).isEqualTo(1);
    assertThat(outboxJpa.count()).isEqualTo(1);
    assertThat(outboxJpa.findByPublishedAtIsNullOrderByCreatedAtAsc()).hasSize(1);
  }
}
