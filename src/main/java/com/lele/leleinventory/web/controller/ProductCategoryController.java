package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.entity.ProductCategory;
import com.lele.leleinventory.repository.ProductCategoryRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/product-categories")
public class ProductCategoryController {
  private final ProductCategoryRepository productCategoryRepository;

  public ProductCategoryController(ProductCategoryRepository productCategoryRepository) {
    this.productCategoryRepository = productCategoryRepository;
  }

  @GetMapping
  public String list(Model model) {
    List<ProductCategory> categories = productCategoryRepository.findAll();
    model.addAttribute("categories", categories);
    return "product_categories/list";
  }

  @GetMapping("/new")
  public String newForm() {
    return "product_categories/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String productCategoryId,
      @RequestParam String productCategoryName
  ) {
    ProductCategory category = new ProductCategory();
    category.setProductCategoryId(productCategoryId);
    category.setProductCategoryName(productCategoryName);
    productCategoryRepository.save(category);
    return "redirect:/product-categories";
  }
}

