package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.InboundOrder;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.Product;
import com.lele.leleinventory.repository.InboundOrderRepository;
import com.lele.leleinventory.repository.ProductRepository;
import com.lele.leleinventory.service.AuditService;
import com.lele.leleinventory.service.InboundOrderService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InboundOrderApiController {
  private final InboundOrderRepository inboundOrderRepository;
  private final ProductRepository productRepository;
  private final InboundOrderService inboundOrderService;
  private final AuditService auditService;

  public InboundOrderApiController(
      InboundOrderRepository inboundOrderRepository,
      ProductRepository productRepository,
      InboundOrderService inboundOrderService,
      AuditService auditService
  ) {
    this.inboundOrderRepository = inboundOrderRepository;
    this.productRepository = productRepository;
    this.inboundOrderService = inboundOrderService;
    this.auditService = auditService;
  }

  @GetMapping("/inbound-orders")
  @Transactional(readOnly = true)
  public List<InboundOrderDto> list() {
    return inboundOrderRepository.findAll().stream().map(InboundOrderDto::from).toList();
  }

  @PostMapping("/inbound-orders")
  public InboundOrderDto create(@RequestBody InboundOrderRequest req) {
    try {
      Instant inputTime = UtcTimes.orNow(req.inputTime());

      InboundOrder inboundOrder = new InboundOrder();
      inboundOrder.setInboundOrderId(req.inboundOrderId());
      inboundOrder.setInboundOrderDate(req.inboundOrderDate());
      inboundOrder.setInboundTrackingNo(req.inboundTrackingNo());
      inboundOrder.setInboundShipCarrier(req.inboundShipCarrier());
      inboundOrder.setInboundSourceName(req.inboundSourceName());
      inboundOrder.setArrived(req.isArrived());
      inboundOrder.setNote(req.note());
      inboundOrder.setInputTime(inputTime);

      List<Item> items = req.items().stream().map(i -> {
        Product product = productRepository.findById(i.productId()).orElseThrow();
        Item item = new Item();
        item.setProduct(product);
        item.setOfficialListingPrice(i.officialListingPrice());
        item.setActualCost(i.actualCost());
        item.setItemSellingPrice(i.itemSellingPrice());
        item.setItemActualSellingPrice(i.itemActualSellingPrice());
        item.setItemStatus(ItemStatus.INBOUND);
        item.setNote(i.note());
        item.setInputTime(UtcTimes.orNow(i.inputTime() != null ? i.inputTime() : inputTime));
        return item;
      }).toList();

      InboundOrder saved = inboundOrderService.createInboundOrder(inboundOrder, items);
      InboundOrderDto dto = InboundOrderDto.from(saved);
      auditService.success(
          AuditAction.CREATE,
          AuditEntityType.INBOUND_ORDER,
          saved.getId(),
          saved.getInboundOrderId(),
          "创建入库单 " + saved.getInboundOrderId() + "，items=" + items.size(),
          null,
          AuditService.mapOf(
              "order", dto,
              "itemCount", items.size(),
              "itemProductIds", items.stream().map(it -> it.getProduct().getId()).toList(),
              "request", req
          )
      );
      return dto;
    } catch (RuntimeException ex) {
      auditService.failure(
          AuditAction.CREATE,
          AuditEntityType.INBOUND_ORDER,
          null,
          req.inboundOrderId(),
          "创建入库单失败",
          null,
          req,
          ex.getMessage()
      );
      throw ex;
    }
  }

  public record InboundOrderDto(
      Long id,
      String inboundOrderId,
      LocalDate inboundOrderDate,
      String inboundTrackingNo,
      String inboundShipCarrier,
      String inboundSourceName,
      boolean arrived,
      String note,
      Instant inputTime
  ) {
    static InboundOrderDto from(InboundOrder o) {
      return new InboundOrderDto(
          o.getId(),
          o.getInboundOrderId(),
          o.getInboundOrderDate(),
          o.getInboundTrackingNo(),
          o.getInboundShipCarrier(),
          o.getInboundSourceName(),
          o.isArrived(),
          o.getNote(),
          o.getInputTime()
      );
    }
  }

  public record InboundOrderRequest(
      String inboundOrderId,
      LocalDate inboundOrderDate,
      String inboundTrackingNo,
      String inboundShipCarrier,
      String inboundSourceName,
      boolean isArrived,
      String note,
      Instant inputTime,
      List<ItemCreateRequest> items
  ) {}

  public record ItemCreateRequest(
      Long productId,
      BigDecimal officialListingPrice,
      BigDecimal actualCost,
      BigDecimal itemSellingPrice,
      BigDecimal itemActualSellingPrice,
      String note,
      Instant inputTime
  ) {}
}
