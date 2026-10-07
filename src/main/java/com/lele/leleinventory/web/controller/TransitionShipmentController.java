package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.ItemStatus;
import com.lele.leleinventory.domain.entity.Item;
import com.lele.leleinventory.domain.entity.TransitionShipment;
import com.lele.leleinventory.repository.ItemRepository;
import com.lele.leleinventory.repository.TransitionShipmentRepository;
import com.lele.leleinventory.service.TransitionShipmentService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/transition-shipments")
public class TransitionShipmentController {
  private final TransitionShipmentRepository transitionShipmentRepository;
  private final ItemRepository itemRepository;
  private final TransitionShipmentService transitionShipmentService;

  public TransitionShipmentController(
      TransitionShipmentRepository transitionShipmentRepository,
      ItemRepository itemRepository,
      TransitionShipmentService transitionShipmentService
  ) {
    this.transitionShipmentRepository = transitionShipmentRepository;
    this.itemRepository = itemRepository;
    this.transitionShipmentService = transitionShipmentService;
  }

  @GetMapping
  public String list(Model model) {
    List<TransitionShipment> shipments = transitionShipmentRepository.findAll();
    model.addAttribute("shipments", shipments);
    model.addAttribute("eligibleItems", itemRepository.findAllByItemStatus(ItemStatus.INBOUND));
    return "transition_shipments/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    List<Item> eligibleItems = itemRepository.findAllByItemStatus(ItemStatus.INBOUND);
    model.addAttribute("eligibleItems", eligibleItems);
    return "transition_shipments/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String shipmentId,
      @RequestParam(required = false) String transitionTrackingNo,
      @RequestParam(required = false) String carrier,
      @RequestParam(required = false) BigDecimal shippingCost,
      @RequestParam(defaultValue = "false") boolean isArrived,
      @RequestParam(required = false) String note,
      @RequestParam List<Long> itemIds
  ) {
    TransitionShipment transitionShipment = new TransitionShipment();
    transitionShipment.setShipmentId(shipmentId);
    transitionShipment.setTransitionTrackingNo(transitionTrackingNo);
    transitionShipment.setCarrier(carrier);
    transitionShipment.setShippingCost(shippingCost);
    transitionShipment.setArrived(isArrived);
    transitionShipment.setNote(note);

    transitionShipmentService.createAndAssign(transitionShipment, itemIds);
    return "redirect:/transition-shipments";
  }
}

