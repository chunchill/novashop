package com.novashop.paymentrefund.infrastructure.persistence;

import com.novashop.paymentrefund.application.port.RefundRepository;
import com.novashop.paymentrefund.domain.refund.Refund;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRefundRepository implements RefundRepository {

  private final SpringDataRefundJpaRepository jpa;

  public JpaRefundRepository(SpringDataRefundJpaRepository jpa) {
    this.jpa = Objects.requireNonNull(jpa, "jpa");
  }

  @Override
  public Optional<Refund> findById(String id) {
    return jpa.findById(id).map(RefundMapper::toDomain);
  }

  @Override
  public void save(Refund refund) {
    Objects.requireNonNull(refund, "refund");
    Optional<RefundEntity> existing = jpa.findById(refund.id());
    if (existing.isPresent()) {
      RefundEntity entity = existing.get();
      RefundMapper.copyToEntity(refund, entity);
      jpa.save(entity);
    } else {
      jpa.save(RefundMapper.toEntity(refund));
    }
  }
}
