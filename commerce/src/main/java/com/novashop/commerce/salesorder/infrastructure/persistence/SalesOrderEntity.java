package com.novashop.commerce.salesorder.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_order")
public class SalesOrderEntity {

  @Id
  @Column(name = "order_id", nullable = false, length = 64)
  private String orderId;

  @Column(name = "payment_id", nullable = false, length = 64)
  private String paymentId;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "refundable_total", nullable = false, precision = 19, scale = 2)
  private BigDecimal refundableTotal;

  @Column(name = "currency", nullable = false, length = 8)
  private String currency;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<OrderLineEntity> lines = new ArrayList<>();

  protected SalesOrderEntity() {}

  public SalesOrderEntity(
      String orderId,
      String paymentId,
      BigDecimal totalAmount,
      BigDecimal refundableTotal,
      String currency) {
    this.orderId = orderId;
    this.paymentId = paymentId;
    this.totalAmount = totalAmount;
    this.refundableTotal = refundableTotal;
    this.currency = currency;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public BigDecimal getRefundableTotal() {
    return refundableTotal;
  }

  public String getCurrency() {
    return currency;
  }

  public List<OrderLineEntity> getLines() {
    return lines;
  }

  public void setLines(List<OrderLineEntity> lines) {
    this.lines = lines;
  }
}
