package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.entity.InboundOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
  Optional<InboundOrder> findByInboundOrderId(String inboundOrderId);
}

