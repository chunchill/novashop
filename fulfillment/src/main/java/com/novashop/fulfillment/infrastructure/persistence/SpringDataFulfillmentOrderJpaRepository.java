package com.novashop.fulfillment.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFulfillmentOrderJpaRepository
    extends JpaRepository<FulfillmentOrderEntity, String> {

  Optional<FulfillmentOrderEntity> findByOrderId(String orderId);
}
