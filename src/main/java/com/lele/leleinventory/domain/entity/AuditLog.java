package com.lele.leleinventory.domain.entity;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 操作发生时间（UTC） */
  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(nullable = false, length = 64)
  private String username;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AuditAction action;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 64)
  private AuditEntityType entityType;

  /** 数据库主键，字符串便于统一查询 */
  @Column(name = "entity_id", length = 64)
  private String entityId;

  /** 业务编码，如商品编码、入库单号 */
  @Column(name = "business_key", length = 128)
  private String businessKey;

  @Column(nullable = false, length = 500)
  private String summary;

  @Lob
  @Column(name = "before_json", columnDefinition = "LONGTEXT")
  private String beforeJson;

  @Lob
  @Column(name = "after_json", columnDefinition = "LONGTEXT")
  private String afterJson;

  @Column(name = "request_path", length = 255)
  private String requestPath;

  @Column(nullable = false)
  private boolean success = true;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @PrePersist
  void prePersist() {
    if (occurredAt == null) {
      occurredAt = Instant.now();
    }
    if (username == null || username.isBlank()) {
      username = "unknown";
    }
  }
}
