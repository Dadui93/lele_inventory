package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Buyer;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.OutboundOrder;
import com.lele.leleinventory.repository.BuyerRepository;
import com.lele.leleinventory.repository.ItemRepository;
import com.lele.leleinventory.repository.OutboundOrderRepository;
import com.lele.leleinventory.service.OutboundOrderService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/outbound-orders")
public class OutboundOrderController {
  private final OutboundOrderRepository outboundOrderRepository;
  private final ItemRepository itemRepository;
  private final BuyerRepository buyerRepository;
  private final OutboundOrderService outboundOrderService;

  public OutboundOrderController(
      OutboundOrderRepository outboundOrderRepository,
      ItemRepository itemRepository,
      BuyerRepository buyerRepository,
      OutboundOrderService outboundOrderService
  ) {
    this.outboundOrderRepository = outboundOrderRepository;
    this.itemRepository = itemRepository;
    this.buyerRepository = buyerRepository;
    this.outboundOrderService = outboundOrderService;
  }

  @GetMapping
  public String list(Model model) {
    List<OutboundOrder> orders = outboundOrderRepository.findAll();
    model.addAttribute("orders", orders);
    model.addAttribute("buyers", buyerRepository.findAll());
    model.addAttribute("eligibleItems", itemRepository.findAllByItemStatus(ItemStatus.IN_TRANSITION));
    return "outbound_orders/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    List<Buyer> buyers = buyerRepository.findAll();
    List<Item> eligibleItems = itemRepository.findAllByItemStatus(ItemStatus.IN_TRANSITION);
    model.addAttribute("buyers", buyers);
    model.addAttribute("eligibleItems", eligibleItems);
    return "outbound_orders/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String outboundOrderId,
      @RequestParam Long buyerId,
      @RequestParam(defaultValue = "false") boolean isArrived,
      @RequestParam(required = false) String note,
      @RequestParam List<Long> itemIds
  ) {
    Buyer buyer = buyerRepository.findById(buyerId).orElseThrow();

    OutboundOrder outboundOrder = new OutboundOrder();
    outboundOrder.setOutboundOrderId(outboundOrderId);
    outboundOrder.setBuyer(buyer);
    outboundOrder.setArrived(isArrived);
    outboundOrder.setNote(note);

    outboundOrderService.createAndAssign(outboundOrder, itemIds);
    return "redirect:/outbound-orders";
  }
}

