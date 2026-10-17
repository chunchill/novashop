package com.novashop.fulfillment.infrastructure.persistence;

import com.novashop.fulfillment.application.port.FulfillmentOrderRepository;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentStatus;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaFulfillmentOrderRepository implements FulfillmentOrderRepository {

  private final SpringDataFulfillmentOrderJpaRepository jpa;

  public JpaFulfillmentOrderRepository(SpringDataFulfillmentOrderJpaRepository jpa) {
    this.jpa = Objects.requireNonNull(jpa, "jpa");
  }

  @Override
  public Optional<FulfillmentOrder> findByOrderId(String orderId) {
    return jpa.findByOrderId(orderId).map(this::toDomain);
  }

  @Override
  public void save(FulfillmentOrder order) {
    Objects.requireNonNull(order, "order");
    jpa.save(
        new FulfillmentOrderEntity(
            order.id(),
            order.orderId(),
            order.status().name(),
            order.warehouseCode(),
            order.rejectReason()));
  }

  private FulfillmentOrder toDomain(FulfillmentOrderEntity e) {
    return FulfillmentOrder.reconstitute(
        e.getId(),
        e.getOrderId(),
        FulfillmentStatus.valueOf(e.getStatus()),
        e.getWarehouseCode(),
        e.getRejectReason());
  }
}
