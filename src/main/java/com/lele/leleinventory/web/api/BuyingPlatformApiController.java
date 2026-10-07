package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.BuyingPlatform;
import com.lele.leleinventory.repository.BuyingPlatformRepository;
import com.lele.leleinventory.service.AuditService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuyingPlatformApiController {
  private final BuyingPlatformRepository buyingPlatformRepository;
  private final AuditService auditService;

  public BuyingPlatformApiController(BuyingPlatformRepository buyingPlatformRepository, AuditService auditService) {
    this.buyingPlatformRepository = buyingPlatformRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/buying-platforms")
  public List<BuyingPlatform> list() {
    return buyingPlatformRepository.findAll();
  }

  @PostMapping("/api/buying-platforms")
  public BuyingPlatform create(@RequestBody BuyingPlatformRequest req) {
    try {
      BuyingPlatform platform = new BuyingPlatform();
      platform.setBuyingPlatformId(req.buyingPlatformId());
      platform.setBuyPlatformName(req.buyPlatformName());
      platform.setInputTime(UtcTimes.orNow(req.inputTime()));
      BuyingPlatform saved = buyingPlatformRepository.save(platform);
      auditService.success(
          AuditAction.CREATE,
          AuditEntityType.BUYING_PLATFORM,
          saved.getId(),
          saved.getBuyingPlatformId(),
          "创建平台 " + saved.getBuyingPlatformId() + " / " + saved.getBuyPlatformName(),
          null,
          snapshot(saved)
      );
      return saved;
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.CREATE, AuditEntityType.BUYING_PLATFORM, null, req.buyingPlatformId(), "创建平台失败", null, req, ex.getMessage());
      throw ex;
    }
  }

  @PutMapping("/api/buying-platforms/{id}")
  public BuyingPlatform update(@PathVariable Long id, @RequestBody BuyingPlatformRequest req) {
    BuyingPlatform platform = buyingPlatformRepository.findById(id).orElseThrow();
    var before = snapshot(platform);
    try {
      platform.setBuyingPlatformId(req.buyingPlatformId());
      platform.setBuyPlatformName(req.buyPlatformName());
      if (req.inputTime() != null) {
        platform.setInputTime(req.inputTime());
      }
      BuyingPlatform saved = buyingPlatformRepository.save(platform);
      auditService.success(AuditAction.UPDATE, AuditEntityType.BUYING_PLATFORM, id, saved.getBuyingPlatformId(), "更新平台 " + saved.getBuyingPlatformId(), before, snapshot(saved));
      return saved;
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.UPDATE, AuditEntityType.BUYING_PLATFORM, id, req.buyingPlatformId(), "更新平台失败", before, req, ex.getMessage());
      throw ex;
    }
  }

  @DeleteMapping("/api/buying-platforms/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    BuyingPlatform platform = buyingPlatformRepository.findById(id).orElseThrow();
    var before = snapshot(platform);
    try {
      buyingPlatformRepository.deleteById(id);
      auditService.success(AuditAction.DELETE, AuditEntityType.BUYING_PLATFORM, id, platform.getBuyingPlatformId(), "删除平台 " + platform.getBuyingPlatformId(), before, null);
      return ResponseEntity.ok().build();
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.DELETE, AuditEntityType.BUYING_PLATFORM, id, platform.getBuyingPlatformId(), "删除平台失败", before, null, ex.getMessage());
      throw ex;
    }
  }

  private static Object snapshot(BuyingPlatform p) {
    return AuditService.mapOf(
        "id", p.getId(),
        "buyingPlatformId", p.getBuyingPlatformId(),
        "buyPlatformName", p.getBuyPlatformName(),
        "inputTime", p.getInputTime()
    );
  }

  public record BuyingPlatformRequest(String buyingPlatformId, String buyPlatformName, Instant inputTime) {}
}
