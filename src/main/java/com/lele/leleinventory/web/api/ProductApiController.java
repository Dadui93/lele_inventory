package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.Product;
import com.lele.leleinventory.domain.entity.ProductCategory;
import com.lele.leleinventory.repository.ProductCategoryRepository;
import com.lele.leleinventory.repository.ProductRepository;
import com.lele.leleinventory.service.AuditService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductApiController {
  private final ProductRepository productRepository;
  private final ProductCategoryRepository productCategoryRepository;
  private final AuditService auditService;

  public ProductApiController(
      ProductRepository productRepository,
      ProductCategoryRepository productCategoryRepository,
      AuditService auditService
  ) {
    this.productRepository = productRepository;
    this.productCategoryRepository = productCategoryRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/products")
  @Transactional(readOnly = true)
  public List<ProductDto> list() {
    return productRepository.findAll().stream().map(ProductDto::from).toList();
  }

  @PostMapping("/api/products")
  public ProductDto create(@RequestBody ProductRequest req) {
    try {
      ProductCategory category = productCategoryRepository.findById(req.productCategoryId()).orElseThrow();
      Product product = new Product();
      product.setProductId(req.productId());
      product.setProductName(req.productName());
      product.setManufacturingName(req.manufacturingName());
      product.setProductCategory(category);
      product.setInputTime(UtcTimes.orNow(req.inputTime()));
      Product saved = productRepository.save(product);
      ProductDto dto = ProductDto.from(saved);
      auditService.success(
          AuditAction.CREATE,
          AuditEntityType.PRODUCT,
          saved.getId(),
          saved.getProductId(),
          "创建商品 " + saved.getProductId() + " / " + saved.getProductName(),
          null,
          dto
      );
      return dto;
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.CREATE, AuditEntityType.PRODUCT, null, req.productId(), "创建商品失败", null, req, ex.getMessage());
      throw ex;
    }
  }

  @PutMapping("/api/products/{id}")
  public ProductDto update(@PathVariable Long id, @RequestBody ProductRequest req) {
    Product product = productRepository.findById(id).orElseThrow();
    ProductDto before = ProductDto.from(product);
    try {
      ProductCategory category = productCategoryRepository.findById(req.productCategoryId()).orElseThrow();
      product.setProductId(req.productId());
      product.setProductName(req.productName());
      product.setManufacturingName(req.manufacturingName());
      product.setProductCategory(category);
      if (req.inputTime() != null) {
        product.setInputTime(req.inputTime());
      }
      ProductDto after = ProductDto.from(productRepository.save(product));
      auditService.success(AuditAction.UPDATE, AuditEntityType.PRODUCT, id, after.productId(), "更新商品 " + after.productId(), before, after);
      return after;
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.UPDATE, AuditEntityType.PRODUCT, id, req.productId(), "更新商品失败", before, req, ex.getMessage());
      throw ex;
    }
  }

  @DeleteMapping("/api/products/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    Product product = productRepository.findById(id).orElseThrow();
    ProductDto before = ProductDto.from(product);
    try {
      productRepository.deleteById(id);
      auditService.success(AuditAction.DELETE, AuditEntityType.PRODUCT, id, product.getProductId(), "删除商品 " + product.getProductId(), before, null);
      return ResponseEntity.ok().build();
    } catch (RuntimeException ex) {
      auditService.failure(AuditAction.DELETE, AuditEntityType.PRODUCT, id, product.getProductId(), "删除商品失败", before, null, ex.getMessage());
      throw ex;
    }
  }

  public record ProductDto(
      Long id,
      String productId,
      String productName,
      String manufacturingName,
      Long productCategoryId,
      String productCategoryName,
      Instant inputTime
  ) {
    static ProductDto from(Product p) {
      return new ProductDto(
          p.getId(),
          p.getProductId(),
          p.getProductName(),
          p.getManufacturingName(),
          p.getProductCategory().getId(),
          p.getProductCategory().getProductCategoryName(),
          p.getInputTime()
      );
    }
  }

  public record ProductRequest(
      String productId,
      String productName,
      String manufacturingName,
      Long productCategoryId,
      Instant inputTime
  ) {}
}
