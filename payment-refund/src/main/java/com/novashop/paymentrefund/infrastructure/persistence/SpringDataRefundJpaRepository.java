package com.novashop.paymentrefund.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRefundJpaRepository extends JpaRepository<RefundEntity, String> {}
