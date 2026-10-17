package com.novashop.commerce.salesorder.infrastructure.persistence;

import com.novashop.commerce.salesorder.application.port.SalesOrderRepository;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSalesOrderRepository implements SalesOrderRepository {

  private final SpringDataSalesOrderJpaRepository jpa;

  public JpaSalesOrderRepository(SpringDataSalesOrderJpaRepository jpa) {
    this.jpa = Objects.requireNonNull(jpa, "jpa");
  }

  @Override
  public void save(SalesOrder order) {
    jpa.save(SalesOrderMapper.toEntity(order));
  }

  @Override
  public Optional<SalesOrder> findById(String orderId) {
    return jpa.findById(orderId).map(SalesOrderMapper::toDomain);
  }
}
