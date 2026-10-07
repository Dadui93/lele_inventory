package com.lele.leleinventory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inbound_order")
public class InboundOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "inbound_order_id", nullable = false, unique = true, length = 64)
  private String inboundOrderId;

  @Column(name = "inbound_order_date")
  private LocalDate inboundOrderDate;

  @Column(name = "inbound_tracking_no", length = 128)
  private String inboundTrackingNo;

  @Column(name = "inbound_ship_carrier", length = 128)
  private String inboundShipCarrier;

  @Column(name = "inbound_source_name", length = 255)
  private String inboundSourceName;

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
