package com.novashop.commerce.salesorder.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_line")
public class OrderLineEntity {

  @Id
  @Column(name = "id", nullable = false, length = 128)
  private String id;

  @Column(name = "line_id", nullable = false, length = 64)
  private String lineId;

  @Column(name = "sku_id", nullable = false, length = 64)
  private String skuId;

  @Column(name = "sku_name", nullable = false, length = 256)
  private String skuName;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "currency", nullable = false, length = 8)
  private String currency;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private SalesOrderEntity order;

  protected OrderLineEntity() {}

  public OrderLineEntity(
      String id,
      String lineId,
      String skuId,
      String skuName,
      int quantity,
      BigDecimal unitPrice,
      String currency,
      SalesOrderEntity order) {
    this.id = id;
    this.lineId = lineId;
    this.skuId = skuId;
    this.skuName = skuName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.currency = currency;
    this.order = order;
  }

  public String getId() {
    return id;
  }

  public String getLineId() {
    return lineId;
  }

  public String getSkuId() {
    return skuId;
  }

  public String getSkuName() {
    return skuName;
  }

  public int getQuantity() {
    return quantity;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public String getCurrency() {
    return currency;
  }
}
