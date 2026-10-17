package com.novashop.commerce.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "catalog_sku")
public class SkuEntity {

  @Id
  @Column(name = "sku_id", nullable = false, length = 64)
  private String skuId;

  @Column(name = "name", nullable = false, length = 256)
  private String name;

  @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "currency", nullable = false, length = 8)
  private String currency;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  protected SkuEntity() {}

  public SkuEntity(
      String skuId, String name, BigDecimal unitPrice, String currency, ProductEntity product) {
    this.skuId = skuId;
    this.name = name;
    this.unitPrice = unitPrice;
    this.currency = currency;
    this.product = product;
  }

  public String getSkuId() {
    return skuId;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public String getCurrency() {
    return currency;
  }

  public ProductEntity getProduct() {
    return product;
  }
}
