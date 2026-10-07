package com.lele.leleinventory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "buying_platform")
public class BuyingPlatform {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "buying_platform_id", nullable = false, unique = true, length = 64)
  private String buyingPlatformId;

  @Column(name = "buy_platform_name", nullable = false, length = 255)
  private String buyPlatformName;

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
