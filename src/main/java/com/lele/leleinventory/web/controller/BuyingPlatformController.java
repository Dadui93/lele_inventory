package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.entity.BuyingPlatform;
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
@RequestMapping("/buying-platforms")
public class BuyingPlatformController {
  private final BuyingPlatformRepository buyingPlatformRepository;

  public BuyingPlatformController(BuyingPlatformRepository buyingPlatformRepository) {
    this.buyingPlatformRepository = buyingPlatformRepository;
  }

  @GetMapping
  public String list(Model model) {
    List<BuyingPlatform> platforms = buyingPlatformRepository.findAll();
    model.addAttribute("platforms", platforms);
    return "buying_platforms/list";
  }

  @GetMapping("/new")
  public String newForm() {
    return "buying_platforms/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String buyingPlatformId,
      @RequestParam String buyPlatformName
  ) {
    BuyingPlatform platform = new BuyingPlatform();
    platform.setBuyingPlatformId(buyingPlatformId);
    platform.setBuyPlatformName(buyPlatformName);
    buyingPlatformRepository.save(platform);
    return "redirect:/buying-platforms";
  }
}

