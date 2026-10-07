package com.lele.leleinventory.service;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.InboundOrder;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.repository.InboundOrderRepository;
import com.lele.leleinventory.repository.ItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboundOrderService {
  private final InboundOrderRepository inboundOrderRepository;
  private final ItemRepository itemRepository;

  public InboundOrderService(InboundOrderRepository inboundOrderRepository, ItemRepository itemRepository) {
    this.inboundOrderRepository = inboundOrderRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public InboundOrder createInboundOrder(InboundOrder inboundOrder, List<Item> items) {
    if (inboundOrder == null || inboundOrder.getInboundOrderId() == null || inboundOrder.getInboundOrderId().isBlank()) {
      throw new BusinessValidationException("inboundOrderId is required");
    }
    if (items == null || items.isEmpty()) {
      throw new BusinessValidationException("inbound order must have at least 1 item");
    }

    inboundOrder = inboundOrderRepository.save(inboundOrder);

    for (Item item : items) {
      if (item.getProduct() == null) {
        throw new BusinessValidationException("item.product is required");
      }
      if (item.getItemStatus() != ItemStatus.INBOUND) {
        throw new BusinessValidationException("inbound items must be INBOUND status");
      }
      item.setInboundOrder(inboundOrder);
      item.setTransitionShipment(null);
      item.setOutboundOrder(null);
    }

    itemRepository.saveAll(items);
    return inboundOrder;
  }
}

