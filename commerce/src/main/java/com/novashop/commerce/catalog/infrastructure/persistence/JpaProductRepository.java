package com.novashop.commerce.catalog.infrastructure.persistence;

import com.novashop.commerce.catalog.application.port.ProductRepository;
import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductRepository implements ProductRepository {

  private final SpringDataProductJpaRepository jpa;

  public JpaProductRepository(SpringDataProductJpaRepository jpa) {
    this.jpa = Objects.requireNonNull(jpa, "jpa");
  }

  @Override
  public List<Product> findAll() {
    return jpa.findAll().stream().map(ProductMapper::toDomain).toList();
  }

  @Override
  public Optional<Product> findById(String productId) {
    return jpa.findById(productId).map(ProductMapper::toDomain);
  }

  @Override
  public Optional<Sku> findSkuById(String skuId) {
    return jpa.findSkuById(skuId).map(ProductMapper::toSku);
  }

  @Override
  public void save(Product product) {
    jpa.save(ProductMapper.toEntity(product));
  }
}
