package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
  List<Item> findAllByItemStatus(ItemStatus itemStatus);

  List<Item> findAllByProduct_Id(Long productId);

  List<Item> findAllByInboundOrder_Id(Long inboundOrderId);

  List<Item> findAllByTransitionShipment_Id(Long transitionShipmentId);

  List<Item> findAllByOutboundOrder_Id(Long outboundOrderId);
}

