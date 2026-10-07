package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.entity.Buyer;
import com.lele.leleinventory.domain.entity.BuyingPlatform;
import com.lele.leleinventory.repository.BuyerRepository;
import com.lele.leleinventory.repository.BuyingPlatformRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/buyers")
public class BuyerController {
  private final BuyerRepository buyerRepository;
  private final BuyingPlatformRepository buyingPlatformRepository;

  public BuyerController(BuyerRepository buyerRepository, BuyingPlatformRepository buyingPlatformRepository) {
    this.buyerRepository = buyerRepository;
    this.buyingPlatformRepository = buyingPlatformRepository;
  }

  @GetMapping
  public String list(Model model) {
    List<Buyer> buyers = buyerRepository.findAll();
    model.addAttribute("buyers", buyers);
    return "buyers/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    List<BuyingPlatform> platforms = buyingPlatformRepository.findAll();
    model.addAttribute("platforms", platforms);
    return "buyers/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String buyerId,
      @RequestParam String buyerName,
      @RequestParam Long buyingPlatformId
  ) {
    BuyingPlatform platform = buyingPlatformRepository.findById(buyingPlatformId).orElseThrow();

    Buyer buyer = new Buyer();
    buyer.setBuyerId(buyerId);
    buyer.setBuyerName(buyerName);
    buyer.setBuyingPlatform(platform);
    buyerRepository.save(buyer);

    return "redirect:/buyers";
  }
}

