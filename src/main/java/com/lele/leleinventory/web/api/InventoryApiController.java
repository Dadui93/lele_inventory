package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.Product;
import com.lele.leleinventory.repository.ItemRepository;
import com.lele.leleinventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryApiController {

  private final ProductRepository productRepository;
  private final ItemRepository itemRepository;

  public InventoryApiController(ProductRepository productRepository, ItemRepository itemRepository) {
    this.productRepository = productRepository;
    this.itemRepository = itemRepository;
  }

  /**
   * 按商品汇总库存：入库中 / 转运中 / 已售，以及当前可用库存（入库中+转运中）。
   */
  @GetMapping("/api/inventory")
  @Transactional(readOnly = true)
  public List<InventoryRow> summary() {
    List<Product> products = productRepository.findAll();
    List<Item> items = itemRepository.findAll();

    Map<Long, EnumMap<ItemStatus, Long>> counts = new HashMap<>();
    Map<Long, BigDecimal> soldRevenue = new HashMap<>();
    Map<Long, BigDecimal> stockCost = new HashMap<>();

    for (Item item : items) {
      Long productPk = item.getProduct().getId();
      counts
          .computeIfAbsent(productPk, id -> {
            EnumMap<ItemStatus, Long> m = new EnumMap<>(ItemStatus.class);
            for (ItemStatus s : ItemStatus.values()) {
              m.put(s, 0L);
            }
            return m;
          })
          .merge(item.getItemStatus(), 1L, Long::sum);

      if (item.getItemStatus() == ItemStatus.OUTBOUND) {
        BigDecimal price = item.getItemActualSellingPrice() != null
            ? item.getItemActualSellingPrice()
            : item.getItemSellingPrice();
        if (price != null) {
          soldRevenue.merge(productPk, price, BigDecimal::add);
        }
      } else if (item.getActualCost() != null) {
        stockCost.merge(productPk, item.getActualCost(), BigDecimal::add);
      }
    }

    List<InventoryRow> rows = new ArrayList<>();
    for (Product p : products) {
      EnumMap<ItemStatus, Long> c = counts.getOrDefault(p.getId(), emptyCounts());
      long inbound = c.get(ItemStatus.INBOUND);
      long inTransition = c.get(ItemStatus.IN_TRANSITION);
      long outbound = c.get(ItemStatus.OUTBOUND);
      long onHand = inbound + inTransition;
      long total = inbound + inTransition + outbound;

      rows.add(new InventoryRow(
          p.getId(),
          p.getProductId(),
          p.getProductName(),
          p.getManufacturingName(),
          p.getProductCategory().getId(),
          p.getProductCategory().getProductCategoryName(),
          inbound,
          inTransition,
          outbound,
          onHand,
          total,
          stockCost.getOrDefault(p.getId(), BigDecimal.ZERO),
          soldRevenue.getOrDefault(p.getId(), BigDecimal.ZERO)
      ));
    }

    rows.sort(Comparator.comparing(InventoryRow::productName, String.CASE_INSENSITIVE_ORDER));
    return rows;
  }

  @GetMapping("/api/inventory/{productPk}/items")
  @Transactional(readOnly = true)
  public List<InventoryItemRow> itemsByProduct(@PathVariable Long productPk) {
    return itemRepository.findAllByProduct_Id(productPk).stream()
        .sorted(Comparator.comparing(Item::getId).reversed())
        .map(InventoryItemRow::from)
        .toList();
  }

  private static EnumMap<ItemStatus, Long> emptyCounts() {
    EnumMap<ItemStatus, Long> m = new EnumMap<>(ItemStatus.class);
    for (ItemStatus s : ItemStatus.values()) {
      m.put(s, 0L);
    }
    return m;
  }

  public record InventoryRow(
      Long id,
      String productId,
      String productName,
      String manufacturingName,
      Long productCategoryId,
      String productCategoryName,
      long inboundCount,
      long inTransitionCount,
      long soldCount,
      long onHandCount,
      long totalCount,
      BigDecimal onHandCost,
      BigDecimal soldRevenue
  ) {}

  public record InventoryItemRow(
      Long id,
      ItemStatus itemStatus,
      BigDecimal actualCost,
      BigDecimal itemSellingPrice,
      BigDecimal itemActualSellingPrice,
      String note,
      Long inboundOrderId,
      Long transitionShipmentId,
      Long outboundOrderId,
      java.time.Instant inputTime
  ) {
    static InventoryItemRow from(Item item) {
      return new InventoryItemRow(
          item.getId(),
          item.getItemStatus(),
          item.getActualCost(),
          item.getItemSellingPrice(),
          item.getItemActualSellingPrice(),
          item.getNote(),
          item.getInboundOrder() != null ? item.getInboundOrder().getId() : null,
          item.getTransitionShipment() != null ? item.getTransitionShipment().getId() : null,
          item.getOutboundOrder() != null ? item.getOutboundOrder().getId() : null,
          item.getInputTime()
      );
    }
  }
}
