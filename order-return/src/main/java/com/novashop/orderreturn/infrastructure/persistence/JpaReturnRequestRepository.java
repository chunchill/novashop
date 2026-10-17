package com.novashop.orderreturn.infrastructure.persistence;

import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaReturnRequestRepository implements ReturnRequestRepository {

  private final SpringDataReturnRequestJpaRepository jpa;

  public JpaReturnRequestRepository(SpringDataReturnRequestJpaRepository jpa) {
    this.jpa = Objects.requireNonNull(jpa, "jpa");
  }

  @Override
  public Optional<ReturnRequest> findById(String id) {
    return jpa.findById(id).map(ReturnRequestMapper::toDomain);
  }

  @Override
  public void save(ReturnRequest returnRequest) {
    Objects.requireNonNull(returnRequest, "returnRequest");
    Optional<ReturnRequestEntity> existing = jpa.findById(returnRequest.id());
    if (existing.isPresent()) {
      ReturnRequestEntity entity = existing.get();
      ReturnRequestMapper.copyToEntity(returnRequest, entity);
      jpa.save(entity);
    } else {
      jpa.save(ReturnRequestMapper.toEntity(returnRequest));
    }
  }

  @Override
  public List<ReturnRequest> findByStatus(ReturnStatus status) {
    Objects.requireNonNull(status, "status");
    return jpa.findByStatus(status.name()).stream().map(ReturnRequestMapper::toDomain).toList();
  }
}
