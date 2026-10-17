package com.novashop.paymentrefund.infrastructure.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOutboxJpaRepository extends JpaRepository<OutboxEntity, String> {

  List<OutboxEntity> findByPublishedAtIsNullOrderByCreatedAtAsc();
}
