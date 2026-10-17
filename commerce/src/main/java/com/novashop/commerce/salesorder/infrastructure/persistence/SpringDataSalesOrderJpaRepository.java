package com.novashop.commerce.salesorder.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSalesOrderJpaRepository extends JpaRepository<SalesOrderEntity, String> {}
