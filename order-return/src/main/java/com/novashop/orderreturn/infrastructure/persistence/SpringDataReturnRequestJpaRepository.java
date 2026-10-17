package com.novashop.orderreturn.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReturnRequestJpaRepository
    extends JpaRepository<ReturnRequestEntity, String> {

  List<ReturnRequestEntity> findByStatus(String status);
}
