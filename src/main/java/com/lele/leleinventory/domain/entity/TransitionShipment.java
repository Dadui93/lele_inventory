package com.lele.leleinventory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transition_shipment")
public class TransitionShipment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "shipment_id", nullable = false, unique = true, length = 64)
  private String shipmentId;

  @Column(name = "transition_tracking_no", length = 128)
  private String transitionTrackingNo;

  @Column(length = 128)
  private String carrier;

  @Column(name = "shipping_cost", precision = 19, scale = 2)
  private BigDecimal shippingCost;

  @Column(name = "is_arrived", nullable = false)
  private boolean isArrived;

  @Column(length = 1000)
  private String note;

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
