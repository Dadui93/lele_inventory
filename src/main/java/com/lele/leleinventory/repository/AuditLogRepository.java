package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.entity.AuditLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  List<AuditLog> findAllByOrderByOccurredAtDescIdDesc(Pageable pageable);

  @Query("""
      select a from AuditLog a
      where (:entityType is null or a.entityType = :entityType)
        and (:action is null or a.action = :action)
        and (
          :q is null or :q = '' or
          lower(a.summary) like lower(concat('%', :q, '%')) or
          lower(coalesce(a.businessKey, '')) like lower(concat('%', :q, '%')) or
          lower(coalesce(a.entityId, '')) like lower(concat('%', :q, '%')) or
          lower(a.username) like lower(concat('%', :q, '%'))
        )
      order by a.occurredAt desc, a.id desc
      """)
  List<AuditLog> search(
      @Param("entityType") AuditEntityType entityType,
      @Param("action") AuditAction action,
      @Param("q") String q,
      Pageable pageable
  );
}
