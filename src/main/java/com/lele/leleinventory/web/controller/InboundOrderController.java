package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.InboundOrder;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.Product;
import com.lele.leleinventory.repository.InboundOrderRepository;
import com.lele.leleinventory.repository.ItemRepository;
import com.lele.leleinventory.repository.ProductRepository;
import com.lele.leleinventory.service.BusinessValidationException;
import com.lele.leleinventory.service.InboundOrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inbound-orders")
public class InboundOrderController {
  private final InboundOrderRepository inboundOrderRepository;
  private final ProductRepository productRepository;
  private final ItemRepository itemRepository;
  private final InboundOrderService inboundOrderService;

  public InboundOrderController(
      InboundOrderRepository inboundOrderRepository,
      ProductRepository productRepository,
      ItemRepository itemRepository,
      InboundOrderService inboundOrderService
  ) {
    this.inboundOrderRepository = inboundOrderRepository;
    this.productRepository = productRepository;
    this.itemRepository = itemRepository;
    this.inboundOrderService = inboundOrderService;
  }

  @GetMapping
  public String list(Model model) {
    List<InboundOrder> orders = inboundOrderRepository.findAll();
    model.addAttribute("orders", orders);
    model.addAttribute("products", productRepository.findAll());
    return "inbound_orders/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    model.addAttribute("products", productRepository.findAll());
    return "inbound_orders/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String inboundOrderId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inboundOrderDate,
      @RequestParam(required = false) String inboundTrackingNo,
      @RequestParam(required = false) String inboundShipCarrier,
      @RequestParam(required = false) String inboundSourceName,
      @RequestParam(defaultValue = "false") boolean isArrived,
      @RequestParam(required = false) String note,
      @RequestParam List<Long> itemProductId,
      @RequestParam List<BigDecimal> officialListingPrice,
      @RequestParam List<BigDecimal> actualCost,
      @RequestParam List<BigDecimal> itemSellingPrice,
      @RequestParam List<BigDecimal> itemActualSellingPrice,
      @RequestParam(required = false) List<String> itemNote
  ) {
    if (itemProductId == null || itemProductId.isEmpty()) {
      throw new BusinessValidationException("inbound order must have items");
    }
    int n = itemProductId.size();
    if (officialListingPrice.size() != n
        || actualCost.size() != n
        || itemSellingPrice.size() != n
        || itemActualSellingPrice.size() != n) {
      throw new BusinessValidationException("item fields length mismatch");
    }

    InboundOrder inboundOrder = new InboundOrder();
    inboundOrder.setInboundOrderId(inboundOrderId);
    inboundOrder.setInboundOrderDate(inboundOrderDate);
    inboundOrder.setInboundTrackingNo(inboundTrackingNo);
    inboundOrder.setInboundShipCarrier(inboundShipCarrier);
    inboundOrder.setInboundSourceName(inboundSourceName);
    inboundOrder.setArrived(isArrived);
    inboundOrder.setNote(note);

    List<Item> items = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Product product = productRepository.findById(itemProductId.get(i)).orElseThrow();

      Item item = new Item();
      item.setProduct(product);
      item.setOfficialListingPrice(officialListingPrice.get(i));
      item.setActualCost(actualCost.get(i));
      item.setItemSellingPrice(itemSellingPrice.get(i));
      item.setItemActualSellingPrice(itemActualSellingPrice.get(i));
      item.setItemStatus(ItemStatus.INBOUND);
      if (itemNote != null && i < itemNote.size()) {
        item.setNote(itemNote.get(i));
      }

      items.add(item);
    }

    inboundOrderService.createInboundOrder(inboundOrder, items);
    return "redirect:/inbound-orders";
  }
}

