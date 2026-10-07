package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.Buyer;
import com.lele.leleinventory.domain.entity.BuyingPlatform;
import com.lele.leleinventory.repository.BuyerRepository;
import com.lele.leleinventory.repository.BuyingPlatformRepository;
import com.lele.leleinventory.service.AuditService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuyerApiController {
  private final BuyerRepository buyerRepository;
  private final BuyingPlatformRepository buyingPlatformRepository;
  private final AuditService auditService;

  public BuyerApiController(
      BuyerRepository buyerRepository,
      BuyingPlatformRepository buyingPlatformRepository,
      AuditService auditService
  ) {
    this.buyerRepository = buyerRepository;
    this.buyingPlatformRepository = buyingPlatformRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/buyers")
  @Transactional(readOnly = true)
  public List<BuyerDto> list() {
    return buyerRepository.findAll().stream().map(BuyerDto::from).toList();
  }

  @PostMapping("/api/buyers")
  public BuyerDto create(@RequestBody BuyerRequest req) {
    try {
      BuyingPlatform platform = buyingPlatformRepository.findById(req.buyingPlatformId()).orElseThrow();
      Buyer buyer = new Buyer();
      buyer.setBuyerId(req.buyerId());
      buyer.setBuyerName(req.buyerName());
      buyer.setBuyingPlatform(platform);
      buyer.setInputTime(UtcTimes.orNow(req.inputTime()));
      BuyerDto dto = BuyerDto.from(buyerRepository.save(buyer));
      auditService.success(AuditAction.CREATE, AuditEntityType.BUYER, dto.id(), dto.buyerId(), "创建客户 " + dto.buyerId() + " / " + dto.buyerName(), null, dto);
      return dto;
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.CREATE, AuditEntityType.BUYER, null, req.buyerId(), "创建客户失败", null, req, ex.getMessage());
      throw ex;
    }
  }

  @PutMapping("/api/buyers/{id}")
  public BuyerDto update(@PathVariable Long id, @RequestBody BuyerRequest req) {
    Buyer buyer = buyerRepository.findById(id).orElseThrow();
    BuyerDto before = BuyerDto.from(buyer);
    try {
      BuyingPlatform platform = buyingPlatformRepository.findById(req.buyingPlatformId()).orElseThrow();
      buyer.setBuyerId(req.buyerId());
      buyer.setBuyerName(req.buyerName());
      buyer.setBuyingPlatform(platform);
      if (req.inputTime() != null) {
        buyer.setInputTime(req.inputTime());
      }
      BuyerDto after = BuyerDto.from(buyerRepository.save(buyer));
      auditService.success(AuditAction.UPDATE, AuditEntityType.BUYER, id, after.buyerId(), "更新客户 " + after.buyerId(), before, after);
      return after;
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.UPDATE, AuditEntityType.BUYER, id, req.buyerId(), "更新客户失败", before, req, ex.getMessage());
      throw ex;
    }
  }

  @DeleteMapping("/api/buyers/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    Buyer buyer = buyerRepository.findById(id).orElseThrow();
    BuyerDto before = BuyerDto.from(buyer);
    try {
      buyerRepository.deleteById(id);
      auditService.success(AuditAction.DELETE, AuditEntityType.BUYER, id, buyer.getBuyerId(), "删除客户 " + buyer.getBuyerId(), before, null);
      return ResponseEntity.ok().build();
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.DELETE, AuditEntityType.BUYER, id, buyer.getBuyerId(), "删除客户失败", before, null, ex.getMessage());
      throw ex;
    }
  }

  public record BuyerDto(
      Long id,
      String buyerId,
      String buyerName,
      Long buyingPlatformId,
      String buyingPlatformName,
      Instant inputTime
  ) {
    static BuyerDto from(Buyer b) {
      return new BuyerDto(
          b.getId(),
          b.getBuyerId(),
          b.getBuyerName(),
          b.getBuyingPlatform().getId(),
          b.getBuyingPlatform().getBuyPlatformName(),
          b.getInputTime()
      );
    }
  }

  public record BuyerRequest(
      String buyerId,
      String buyerName,
      Long buyingPlatformId,
      Instant inputTime
  ) {}
}
