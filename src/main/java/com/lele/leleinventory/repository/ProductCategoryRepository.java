package com.lele.leleinventory.repository;

import com.lele.leleinventory.domain.entity.ProductCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
  Optional<ProductCategory> findByProductCategoryId(String productCategoryId);
}

