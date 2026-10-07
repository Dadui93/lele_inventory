package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.domain.UtcTimes;
import com.lele.leleinventory.domain.entity.ProductCategory;
import com.lele.leleinventory.repository.ProductCategoryRepository;
import com.lele.leleinventory.service.AuditService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductCategoryApiController {
  private final ProductCategoryRepository productCategoryRepository;
  private final AuditService auditService;

  public ProductCategoryApiController(
      ProductCategoryRepository productCategoryRepository,
      AuditService auditService
  ) {
    this.productCategoryRepository = productCategoryRepository;
    this.auditService = auditService;
  }

  @GetMapping("/api/product-categories")
  public List<ProductCategory> list() {
    return productCategoryRepository.findAll();
  }

  @PostMapping("/api/product-categories")
  public ProductCategory create(@RequestBody ProductCategoryRequest req) {
    try {
      ProductCategory category = new ProductCategory();
      category.setProductCategoryId(req.productCategoryId());
      category.setProductCategoryName(req.productCategoryName());
      category.setInputTime(UtcTimes.orNow(req.inputTime()));
      ProductCategory saved = productCategoryRepository.save(category);
      auditService.success(
          AuditAction.CREATE,
          AuditEntityType.PRODUCT_CATEGORY,
          saved.getId(),
          saved.getProductCategoryId(),
          "创建品类 " + saved.getProductCategoryId() + " / " + saved.getProductCategoryName(),
          null,
          snapshot(saved)
      );
      return saved;
    } catch (RuntimeException ex) {
      auditService.failure(
          AuditAction.CREATE,
          AuditEntityType.PRODUCT_CATEGORY,
          null,
          req.productCategoryId(),
          "创建品类失败",
          null,
          req,
          ex.getMessage()
      );
      throw ex;
    }
  }

  @PutMapping("/api/product-categories/{id}")
  public ProductCategory update(@PathVariable Long id, @RequestBody ProductCategoryRequest req) {
    ProductCategory category = productCategoryRepository.findById(id).orElseThrow();
    var before = snapshot(category);
    try {
      category.setProductCategoryId(req.productCategoryId());
      category.setProductCategoryName(req.productCategoryName());
      if (req.inputTime() != null) {
        category.setInputTime(req.inputTime());
      }
      ProductCategory saved = productCategoryRepository.save(category);
      auditService.success(
          AuditAction.UPDATE,
          AuditEntityType.PRODUCT_CATEGORY,
          saved.getId(),
          saved.getProductCategoryId(),
          "更新品类 " + saved.getProductCategoryId(),
          before,
          snapshot(saved)
      );
      return saved;
    } catch (RuntimeException ex) {
      auditService.failure(
          AuditAction.UPDATE,
          AuditEntityType.PRODUCT_CATEGORY,
          id,
          req.productCategoryId(),
          "更新品类失败",
          before,
          req,
          ex.getMessage()
      );
      throw ex;
    }
  }

  @DeleteMapping("/api/product-categories/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    ProductCategory category = productCategoryRepository.findById(id).orElseThrow();
    var before = snapshot(category);
    try {
      productCategoryRepository.deleteById(id);
      auditService.success(
          AuditAction.DELETE,
          AuditEntityType.PRODUCT_CATEGORY,
          id,
          category.getProductCategoryId(),
          "删除品类 " + category.getProductCategoryId(),
          before,
          null
      );
      return ResponseEntity.ok().build();
    } catch (RuntimeException ex) {
      auditService.failure(
          AuditAction.DELETE,
          AuditEntityType.PRODUCT_CATEGORY,
          id,
          category.getProductCategoryId(),
          "删除品类失败",
          before,
          null,
          ex.getMessage()
      );
      throw ex;
    }
  }

  private static Object snapshot(ProductCategory c) {
    return AuditService.mapOf(
        "id", c.getId(),
        "productCategoryId", c.getProductCategoryId(),
        "productCategoryName", c.getProductCategoryName(),
        "inputTime", c.getInputTime()
    );
  }

  public record ProductCategoryRequest(String productCategoryId, String productCategoryName, Instant inputTime) {}
}
