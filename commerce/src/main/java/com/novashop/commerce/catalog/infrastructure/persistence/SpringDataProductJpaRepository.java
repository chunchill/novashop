package com.novashop.commerce.catalog.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProductJpaRepository extends JpaRepository<ProductEntity, String> {

  @Query("select s from SkuEntity s where s.skuId = :skuId")
  Optional<SkuEntity> findSkuById(@Param("skuId") String skuId);
}
