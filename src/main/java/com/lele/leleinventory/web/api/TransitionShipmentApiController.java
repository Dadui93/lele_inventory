package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.TransitionShipment;
import com.lele.leleinventory.repository.TransitionShipmentRepository;
import com.lele.leleinventory.service.AuditService;
import com.lele.leleinventory.service.TransitionShipmentService;
import java.math.BigDecimal;
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
public class TransitionShipmentApiController {
  private final TransitionShipmentRepository transitionShipmentRepository;
  private final TransitionShipmentService transitionShipmentService;
  private final AuditService auditService;

  public TransitionShipmentApiController(
      TransitionShipmentRepository transitionShipmentRepository,
      TransitionShipmentService transitionShipmentService,
      AuditService auditService
  ) {
    this.transitionShipmentRepository = transitionShipmentRepository;
    this.transitionShipmentService = transitionShipmentService;
    this.auditService = auditService;
  }

  @GetMapping("/transition-shipments")
  @Transactional(readOnly = true)
  public List<TransitionShipmentDto> list() {
    return transitionShipmentRepository.findAll().stream().map(TransitionShipmentDto::from).toList();
  }

  @PostMapping("/transition-shipments")
  public TransitionShipmentDto create(@RequestBody TransitionShipmentRequest req) {
    try {
      TransitionShipment s = new TransitionShipment();
      s.setShipmentId(req.shipmentId());
      s.setTransitionTrackingNo(req.transitionTrackingNo());
      s.setCarrier(req.carrier());
      s.setShippingCost(req.shippingCost());
      s.setArrived(req.isArrived());
      s.setNote(req.note());
      s.setInputTime(UtcTimes.orNow(req.inputTime()));

      TransitionShipment saved = transitionShipmentService.createAndAssign(s, req.itemIds());
      TransitionShipmentDto dto = TransitionShipmentDto.from(saved);
      auditService.success(
          AuditAction.CREATE,
          AuditEntityType.TRANSITION_SHIPMENT,
          saved.getId(),
          saved.getShipmentId(),
          "创建转运单 " + saved.getShipmentId() + "，items=" + (req.itemIds() == null ? 0 : req.itemIds().size()),
          null,
          AuditService.mapOf("shipment", dto, "itemIds", req.itemIds())
      );
      return dto;
    } catch (RuntimeException ex) {
      auditService.failure(
          AuditAction.CREATE,
          AuditEntityType.TRANSITION_SHIPMENT,
          null,
          req.shipmentId(),
          "创建转运单失败",
          null,
          req,
          ex.getMessage()
      );
      throw ex;
    }
  }

  public record TransitionShipmentDto(
      Long id,
      String shipmentId,
      String transitionTrackingNo,
      String carrier,
      BigDecimal shippingCost,
      boolean arrived,
      String note,
      Instant inputTime
  ) {
    static TransitionShipmentDto from(TransitionShipment s) {
      return new TransitionShipmentDto(
          s.getId(),
          s.getShipmentId(),
          s.getTransitionTrackingNo(),
          s.getCarrier(),
          s.getShippingCost(),
          s.isArrived(),
          s.getNote(),
          s.getInputTime()
      );
    }
  }

  public record TransitionShipmentRequest(
      String shipmentId,
      String transitionTrackingNo,
      String carrier,
      BigDecimal shippingCost,
      boolean isArrived,
      String note,
      Instant inputTime,
      List<Long> itemIds
  ) {}
}
