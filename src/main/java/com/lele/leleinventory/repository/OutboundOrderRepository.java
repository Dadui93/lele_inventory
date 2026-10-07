package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.entity.OutboundOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long> {
  Optional<OutboundOrder> findByOutboundOrderId(String outboundOrderId);
}

