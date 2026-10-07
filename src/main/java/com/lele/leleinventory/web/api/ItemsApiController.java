package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.repository.ItemRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ItemsApiController {
  private final ItemRepository itemRepository;

  public ItemsApiController(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @GetMapping("/items")
  public List<ItemDto> listByStatus(@RequestParam ItemStatus status) {
    return itemRepository.findAllByItemStatus(status).stream().map(ItemDto::from).toList();
  }

  @GetMapping("/inbound-orders/{inboundOrderId}/items")
  public List<ItemDto> listInboundOrderItems(@PathVariable Long inboundOrderId) {
    return itemRepository.findAllByInboundOrder_Id(inboundOrderId).stream().map(ItemDto::from).toList();
  }

  @GetMapping("/transition-shipments/{transitionShipmentId}/items")
  public List<ItemDto> listTransitionShipmentItems(@PathVariable Long transitionShipmentId) {
    return itemRepository.findAllByTransitionShipment_Id(transitionShipmentId).stream().map(ItemDto::from).toList();
  }

  @GetMapping("/outbound-orders/{outboundOrderId}/items")
  public List<ItemDto> listOutboundOrderItems(@PathVariable Long outboundOrderId) {
    return itemRepository.findAllByOutboundOrder_Id(outboundOrderId).stream().map(ItemDto::from).toList();
  }

  public record ItemDto(
      Long id,
      ItemStatus itemStatus,
      String productCategoryName,
      String productName,
      java.math.BigDecimal officialListingPrice,
      java.math.BigDecimal actualCost,
      java.math.BigDecimal itemSellingPrice,
      java.math.BigDecimal itemActualSellingPrice,
      Long inboundOrderDbId,
      String inboundOrderId,
      String transitionShipmentId,
      String outboundOrderId,
      java.time.Instant inputTime
  ) {
    static ItemDto from(Item item) {
      return new ItemDto(
          item.getId(),
          item.getItemStatus(),
          item.getProduct().getProductCategory().getProductCategoryName(),
          item.getProduct().getProductName(),
          item.getOfficialListingPrice(),
          item.getActualCost(),
          item.getItemSellingPrice(),
          item.getItemActualSellingPrice(),
          item.getInboundOrder().getId(),
          item.getInboundOrder().getInboundOrderId(),
          item.getTransitionShipment() == null ? null : item.getTransitionShipment().getShipmentId(),
          item.getOutboundOrder() == null ? null : item.getOutboundOrder().getOutboundOrderId(),
          item.getInputTime()
      );
    }
  }
}

