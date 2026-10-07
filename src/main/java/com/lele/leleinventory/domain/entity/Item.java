package com.lele.leleinventory.domain.entity;

import com.lele.leleinventory.domain.ItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "item")
public class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "item_official_listing_price", precision = 19, scale = 2)
  private BigDecimal officialListingPrice;

  @Column(name = "item_actual_cost", precision = 19, scale = 2)
  private BigDecimal actualCost;

  @Column(name = "item_selling_price", precision = 19, scale = 2)
  private BigDecimal itemSellingPrice;

  @Column(name = "item_actual_selling_price", precision = 19, scale = 2)
  private BigDecimal itemActualSellingPrice;

  @Column(length = 1000)
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_status", nullable = false, length = 32)
  private ItemStatus itemStatus;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "inbound_order_id", nullable = false)
  private InboundOrder inboundOrder;

  @ManyToOne(optional = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "transition_ship_id")
  private TransitionShipment transitionShipment;

  @ManyToOne(optional = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "outbound_order_id")
  private OutboundOrder outboundOrder;

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

