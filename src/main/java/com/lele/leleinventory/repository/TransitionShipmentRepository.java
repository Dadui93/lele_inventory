package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.entity.TransitionShipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransitionShipmentRepository extends JpaRepository<TransitionShipment, Long> {
  Optional<TransitionShipment> findByShipmentId(String shipmentId);
}

