package com.lele.leleinventory.service;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.TransitionShipment;
import com.lele.leleinventory.repository.ItemRepository;
import com.lele.leleinventory.repository.TransitionShipmentRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransitionShipmentService {
  private final TransitionShipmentRepository transitionShipmentRepository;
  private final ItemRepository itemRepository;

  public TransitionShipmentService(TransitionShipmentRepository transitionShipmentRepository, ItemRepository itemRepository) {
    this.transitionShipmentRepository = transitionShipmentRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public TransitionShipment createAndAssign(TransitionShipment transitionShipment, List<Long> itemIds) {
    if (transitionShipment == null || transitionShipment.getShipmentId() == null || transitionShipment.getShipmentId().isBlank()) {
      throw new BusinessValidationException("shipmentId is required");
    }
    if (itemIds == null || itemIds.isEmpty()) {
      throw new BusinessValidationException("transition shipment must have at least 1 item");
    }

    List<Item> items = itemRepository.findAllById(itemIds);
    if (items.size() != itemIds.size()) {
      throw new BusinessValidationException("some itemIds are invalid");
    }

    List<Long> invalid = new ArrayList<>();
    for (Item item : items) {
      if (item.getItemStatus() != ItemStatus.INBOUND) {
        invalid.add(item.getId());
        continue;
      }
      if (item.getTransitionShipment() != null || item.getOutboundOrder() != null) {
        invalid.add(item.getId());
        continue;
      }
    }
    if (!invalid.isEmpty()) {
      throw new BusinessValidationException("cannot transition items not in INBOUND: " + invalid);
    }

    transitionShipment = transitionShipmentRepository.save(transitionShipment);

    for (Item item : items) {
      item.setTransitionShipment(transitionShipment);
      item.setItemStatus(ItemStatus.IN_TRANSITION);
      itemRepository.save(item);
    }

    return transitionShipment;
  }
}

