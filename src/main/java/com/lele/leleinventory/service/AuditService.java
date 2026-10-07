package com.lele.leleinventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.entity.AuditLog;
import com.lele.leleinventory.repository.AuditLogRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;

  public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
    this.auditLogRepository = auditLogRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void success(
      AuditAction action,
      AuditEntityType entityType,
      Object entityId,
      String businessKey,
      String summary,
      Object before,
      Object after
  ) {
    save(action, entityType, entityId, businessKey, summary, before, after, true, null);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void failure(
      AuditAction action,
      AuditEntityType entityType,
      Object entityId,
      String businessKey,
      String summary,
      Object before,
      Object after,
      String errorMessage
  ) {
    save(action, entityType, entityId, businessKey, summary, before, after, false, errorMessage);
  }

  private void save(
      AuditAction action,
      AuditEntityType entityType,
      Object entityId,
      String businessKey,
      String summary,
      Object before,
      Object after,
      boolean success,
      String errorMessage
  ) {
    AuditLog log = new AuditLog();
    log.setOccurredAt(Instant.now());
    log.setUsername(currentUsername());
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId == null ? null : String.valueOf(entityId));
    log.setBusinessKey(businessKey);
    log.setSummary(summary == null ? action.name() : summary);
    log.setBeforeJson(toJson(before));
    log.setAfterJson(toJson(after));
    log.setRequestPath(currentRequestPath());
    log.setSuccess(success);
    log.setErrorMessage(truncate(errorMessage, 1000));
    auditLogRepository.save(log);
  }

  public static Map<String, Object> mapOf(Object... kv) {
    Map<String, Object> map = new LinkedHashMap<>();
    if (kv == null) {
      return map;
    }
    for (int i = 0; i + 1 < kv.length; i += 2) {
      map.put(String.valueOf(kv[i]), kv[i + 1]);
    }
    return map;
  }

  private String toJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return "{\"serializeError\":\"" + e.getMessage() + "\",\"raw\":\"" + String.valueOf(value).replace("\"", "'") + "\"}";
    }
  }

  private static String currentUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      return "anonymous";
    }
    return auth.getName();
  }

  private static String currentRequestPath() {
    try {
      ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs == null || attrs.getRequest() == null) {
        return null;
      }
      return attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI();
    } catch (Exception e) {
      return null;
    }
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return null;
    }
    return s.length() <= max ? s : s.substring(0, max);
  }
}
