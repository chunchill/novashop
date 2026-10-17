package com.novashop.commerce.catalog.domain.product;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Product {

  private final String productId;
  private final String name;
  private final String description;
  private final List<Sku> skus;

  public Product(String productId, String name, String description, List<Sku> skus) {
    this.productId = Objects.requireNonNull(productId, "productId");
    this.name = Objects.requireNonNull(name, "name");
    this.description = description == null ? "" : description;
    this.skus = List.copyOf(Objects.requireNonNull(skus, "skus"));
    if (this.skus.isEmpty()) {
      throw new IllegalArgumentException("skus");
    }
  }

  public Optional<Sku> findSku(String skuId) {
    return skus.stream().filter(s -> s.skuId().equals(skuId)).findFirst();
  }

  public String productId() {
    return productId;
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public List<Sku> skus() {
    return skus;
  }
}
