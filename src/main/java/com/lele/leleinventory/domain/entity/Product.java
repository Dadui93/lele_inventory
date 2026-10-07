package com.lele.leleinventory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false, unique = true, length = 64)
  private String productId;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "manufacturing_name", length = 255)
  private String manufacturingName;

  @ManyToOne(optional = false)
  @JoinColumn(name = "product_category_id", nullable = false)
  private ProductCategory productCategory;

  /** 录入时间（UTC） */
  @Column(name = "input_time")
  private Instant inputTime;

  @PrePersist
  void prePersist() {
    if (inputTime == null) {
      inputTime = Instant.now();
    }
  }
}
