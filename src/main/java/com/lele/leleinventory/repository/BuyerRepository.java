package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.entity.Buyer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {
  Optional<Buyer> findByBuyerId(String buyerId);
}

