package com.novashop.commerce.salesorder.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "commerce_outbox_message")
public class OutboxEntity {

  @Id
  @Column(name = "id", nullable = false, length = 64)
  private String id;

  @Column(name = "event_type", nullable = false, length = 128)
  private String eventType;

  @Lob
  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  protected OutboxEntity() {}

  public OutboxEntity(String id, String eventType, String payload, Instant createdAt) {
    this.id = id;
    this.eventType = eventType;
    this.payload = payload;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getEventType() {
    return eventType;
  }

  public String getPayload() {
    return payload;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void markPublished(Instant at) {
    this.publishedAt = at;
  }

  public boolean isPublished() {
    return publishedAt != null;
  }
}
