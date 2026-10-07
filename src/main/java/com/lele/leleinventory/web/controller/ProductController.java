package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.domain.entity.Product;
import com.lele.leleinventory.domain.entity.ProductCategory;
import com.lele.leleinventory.repository.ProductCategoryRepository;
import com.lele.leleinventory.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/products")
public class ProductController {
  private final ProductRepository productRepository;
  private final ProductCategoryRepository productCategoryRepository;

  public ProductController(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository) {
    this.productRepository = productRepository;
    this.productCategoryRepository = productCategoryRepository;
  }

  @GetMapping
  public String list(Model model) {
    List<Product> products = productRepository.findAll();
    model.addAttribute("products", products);
    return "products/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    List<ProductCategory> categories = productCategoryRepository.findAll();
    model.addAttribute("categories", categories);
    return "products/new";
  }

  @PostMapping
  @Transactional
  public String create(
      @RequestParam String productId,
      @RequestParam String productName,
      @RequestParam(required = false) String manufacturingName,
      @RequestParam Long productCategoryId
  ) {
    ProductCategory category = productCategoryRepository.findById(productCategoryId).orElseThrow();

    Product product = new Product();
    product.setProductId(productId);
    product.setProductName(productName);
    product.setManufacturingName(manufacturingName);
    product.setProductCategory(category);

    productRepository.save(product);
    return "redirect:/products";
  }
}

