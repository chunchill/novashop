package com.novashop.orderreturn.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "return_request")
public class ReturnRequestEntity {

  @Id
  @Column(name = "id", nullable = false, length = 64)
  private String id;

  @Column(name = "payment_id", nullable = false, length = 64)
  private String paymentId;

  @Column(name = "refundable_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal refundableAmount;

  @Column(name = "refundable_currency", nullable = false, length = 8)
  private String refundableCurrency;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "refund_id", length = 64)
  private String refundId;

  @Column(name = "refund_status", length = 32)
  private String refundStatus;

  @Column(name = "actor_id", length = 64)
  private String actorId;

  @Column(name = "actor_role", length = 32)
  private String actorRole;

  @Column(name = "reason", length = 512)
  private String reason;

  @Column(name = "resolution_code", length = 64)
  private String resolutionCode;

  @Column(name = "refund_amount", precision = 19, scale = 2)
  private BigDecimal refundAmount;

  @Column(name = "refund_currency", length = 8)
  private String refundCurrency;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "return_line", joinColumns = @JoinColumn(name = "return_request_id"))
  private List<ReturnLineEntity> lines = new ArrayList<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "return_refunded_line",
      joinColumns = @JoinColumn(name = "return_request_id"))
  @Column(name = "order_line_id", nullable = false)
  private Set<String> refundedLineIds = new HashSet<>();

  protected ReturnRequestEntity() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public BigDecimal getRefundableAmount() {
    return refundableAmount;
  }

  public void setRefundableAmount(BigDecimal refundableAmount) {
    this.refundableAmount = refundableAmount;
  }

  public String getRefundableCurrency() {
    return refundableCurrency;
  }

  public void setRefundableCurrency(String refundableCurrency) {
    this.refundableCurrency = refundableCurrency;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getRefundId() {
    return refundId;
  }

  public void setRefundId(String refundId) {
    this.refundId = refundId;
  }

  public String getRefundStatus() {
    return refundStatus;
  }

  public void setRefundStatus(String refundStatus) {
    this.refundStatus = refundStatus;
  }

  public String getActorId() {
    return actorId;
  }

  public void setActorId(String actorId) {
    this.actorId = actorId;
  }

  public String getActorRole() {
    return actorRole;
  }

  public void setActorRole(String actorRole) {
    this.actorRole = actorRole;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getResolutionCode() {
    return resolutionCode;
  }

  public void setResolutionCode(String resolutionCode) {
    this.resolutionCode = resolutionCode;
  }

  public BigDecimal getRefundAmount() {
    return refundAmount;
  }

  public void setRefundAmount(BigDecimal refundAmount) {
    this.refundAmount = refundAmount;
  }

  public String getRefundCurrency() {
    return refundCurrency;
  }

  public void setRefundCurrency(String refundCurrency) {
    this.refundCurrency = refundCurrency;
  }

  public List<ReturnLineEntity> getLines() {
    return lines;
  }

  public void setLines(List<ReturnLineEntity> lines) {
    this.lines = lines;
  }

  public Set<String> getRefundedLineIds() {
    return refundedLineIds;
  }

  public void setRefundedLineIds(Set<String> refundedLineIds) {
    this.refundedLineIds = refundedLineIds;
  }
}
