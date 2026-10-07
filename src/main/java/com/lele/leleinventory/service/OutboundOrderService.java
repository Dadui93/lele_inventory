package com.lele.leleinventory.service;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.OutboundOrder;
import com.lele.leleinventory.repository.ItemRepository;
import com.lele.leleinventory.repository.OutboundOrderRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboundOrderService {
  private final OutboundOrderRepository outboundOrderRepository;
  private final ItemRepository itemRepository;

  public OutboundOrderService(OutboundOrderRepository outboundOrderRepository, ItemRepository itemRepository) {
    this.outboundOrderRepository = outboundOrderRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public OutboundOrder createAndAssign(OutboundOrder outboundOrder, List<Long> itemIds) {
    if (outboundOrder == null || outboundOrder.getOutboundOrderId() == null || outboundOrder.getOutboundOrderId().isBlank()) {
      throw new BusinessValidationException("outboundOrderId is required");
    }
    if (outboundOrder.getBuyer() == null) {
      throw new BusinessValidationException("buyer is required");
    }
    if (itemIds == null || itemIds.isEmpty()) {
      throw new BusinessValidationException("outbound order must have at least 1 item");
    }

    List<Item> items = itemRepository.findAllById(itemIds);
    if (items.size() != itemIds.size()) {
      throw new BusinessValidationException("some itemIds are invalid");
    }

    List<Long> invalid = new ArrayList<>();
    for (Item item : items) {
      if (item.getItemStatus() != ItemStatus.IN_TRANSITION) {
        invalid.add(item.getId());
        continue;
      }
      if (item.getTransitionShipment() == null) {
        invalid.add(item.getId());
        continue;
      }
      if (item.getOutboundOrder() != null) {
        invalid.add(item.getId());
      }
    }
    if (!invalid.isEmpty()) {
      throw new BusinessValidationException("cannot outbound items not in IN_TRANSITION: " + invalid);
    }

    outboundOrder = outboundOrderRepository.save(outboundOrder);

    for (Item item : items) {
      item.setOutboundOrder(outboundOrder);
      item.setItemStatus(ItemStatus.OUTBOUND);
      itemRepository.save(item);
    }

    return outboundOrder;
  }
}

