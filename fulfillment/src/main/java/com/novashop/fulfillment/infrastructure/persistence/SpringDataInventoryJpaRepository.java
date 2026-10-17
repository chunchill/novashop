package com.novashop.fulfillment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataInventoryJpaRepository
    extends JpaRepository<InventoryPositionEntity, String> {}
