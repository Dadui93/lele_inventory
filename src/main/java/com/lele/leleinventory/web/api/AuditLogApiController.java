package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.entity.AuditLog;
import com.lele.leleinventory.repository.AuditLogRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditLogApiController {
  private final AuditLogRepository auditLogRepository;

  public AuditLogApiController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @GetMapping("/api/audit-logs")
  @Transactional(readOnly = true)
  public List<AuditLogDto> list(
      @RequestParam(required = false) AuditEntityType entityType,
      @RequestParam(required = false) AuditAction action,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "200") int limit
  ) {
    int size = Math.min(Math.max(limit, 1), 500);
    return auditLogRepository
        .search(entityType, action, q == null ? "" : q.trim(), PageRequest.of(0, size))
        .stream()
        .map(AuditLogDto::from)
        .toList();
  }

  public record AuditLogDto(
      Long id,
      Instant occurredAt,
      String username,
      AuditAction action,
      AuditEntityType entityType,
      String entityId,
      String businessKey,
      String summary,
      String beforeJson,
      String afterJson,
      String requestPath,
      boolean success,
      String errorMessage
  ) {
    static AuditLogDto from(AuditLog a) {
      return new AuditLogDto(
          a.getId(),
          a.getOccurredAt(),
          a.getUsername(),
          a.getAction(),
          a.getEntityType(),
          a.getEntityId(),
          a.getBusinessKey(),
          a.getSummary(),
          a.getBeforeJson(),
          a.getAfterJson(),
          a.getRequestPath(),
          a.isSuccess(),
          a.getErrorMessage()
      );
    }
  }
}
