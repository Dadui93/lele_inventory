package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.entity.BuyingPlatform;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyingPlatformRepository extends JpaRepository<BuyingPlatform, Long> {
  Optional<BuyingPlatform> findByBuyingPlatformId(String buyingPlatformId);
}

