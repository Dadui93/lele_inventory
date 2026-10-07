package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.Buyer;
import com.lele.leleinventory.domain.entity.OutboundOrder;
import com.lele.leleinventory.repository.BuyerRepository;
import com.lele.leleinventory.repository.OutboundOrderRepository;
import com.lele.leleinventory.service.AuditService;
import com.lele.leleinventory.service.OutboundOrderService;
import java.time.Instant;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OutboundOrderApiController {
  private final OutboundOrderRepository outboundOrderRepository;
  private final BuyerRepository buyerRepository;
  private final OutboundOrderService outboundOrderService;
  private final AuditService auditService;

  public OutboundOrderApiController(
      OutboundOrderRepository outboundOrderRepository,
      BuyerRepository buyerRepository,
      OutboundOrderService outboundOrderService,
      AuditService auditService
  ) {
    this.outboundOrderRepository = outboundOrderRepository;
    this.buyerRepository = buyerRepository;
    this.outboundOrderService = outboundOrderService;
    this.auditService = auditService;
  }

  @GetMapping("/outbound-orders")
  @Transactional(readOnly = true)
  public List<OutboundOrderDto> list() {
    return outboundOrderRepository.findAll().stream().map(OutboundOrderDto::from).toList();
  }

  @PostMapping("/outbound-orders")
  public OutboundOrderDto create(@RequestBody OutboundOrderRequest req) {
    try {
      Buyer buyer = buyerRepository.findById(req.buyerId()).orElseThrow();

      OutboundOrder outboundOrder = new OutboundOrder();
      outboundOrder.setOutboundOrderId(req.outboundOrderId());
      outboundOrder.setBuyer(buyer);
      outboundOrder.setArrived(req.isArrived());
      outboundOrder.setNote(req.note());
      outboundOrder.setInputTime(UtcTimes.orNow(req.inputTime()));

      OutboundOrder saved = outboundOrderService.createAndAssign(outboundOrder, req.itemIds());
      OutboundOrderDto dto = OutboundOrderDto.from(saved);
      auditService.success(
          AuditAction.CREATE,
          AuditEntityType.OUTBOUND_ORDER,
          saved.getId(),
          saved.getOutboundOrderId(),
          "创建出库单 " + saved.getOutboundOrderId() + "，buyer=" + buyer.getBuyerId() + "，items=" + (req.itemIds() == null ? 0 : req.itemIds().size()),
          null,
          AuditService.mapOf("order", dto, "itemIds", req.itemIds())
      );
      return dto;
    } catch (RuntimeException ex) {
      auditService.failure(
          AuditAction.CREATE,
          AuditEntityType.OUTBOUND_ORDER,
          null,
          req.outboundOrderId(),
          "创建出库单失败",
          null,
          req,
          ex.getMessage()
      );
      throw ex;
    }
  }

  public record OutboundOrderDto(
      Long id,
      String outboundOrderId,
      Long buyerId,
      String buyerName,
      String buyingPlatformName,
      boolean arrived,
      String note,
      Instant inputTime
  ) {
    static OutboundOrderDto from(OutboundOrder o) {
      return new OutboundOrderDto(
          o.getId(),
          o.getOutboundOrderId(),
          o.getBuyer().getId(),
          o.getBuyer().getBuyerName(),
          o.getBuyer().getBuyingPlatform().getBuyPlatformName(),
          o.isArrived(),
          o.getNote(),
          o.getInputTime()
      );
    }
  }

  public record OutboundOrderRequest(
      String outboundOrderId,
      Long buyerId,
      boolean isArrived,
      String note,
      Instant inputTime,
      List<Long> itemIds
  ) {}
}
