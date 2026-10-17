package com.novashop.commerce.catalog.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "catalog_product")
public class ProductEntity {

  @Id
  @Column(name = "product_id", nullable = false, length = 64)
  private String productId;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "description", nullable = false, length = 1024)
  private String description;

  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<SkuEntity> skus = new ArrayList<>();

  protected ProductEntity() {}

  public ProductEntity(String productId, String name, String description) {
    this.productId = productId;
    this.name = name;
    this.description = description;
  }

  public String getProductId() {
    return productId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<SkuEntity> getSkus() {
    return skus;
  }

  public void setSkus(List<SkuEntity> skus) {
    this.skus = skus;
  }
}
