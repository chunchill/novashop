package com.novashop.commerce.catalog.interfaces.api;

import com.novashop.commerce.catalog.infrastructure.config.CatalogHandlersConfig.GetProductService;
import com.novashop.commerce.catalog.infrastructure.config.CatalogHandlersConfig.ListProductsService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ListProductsService listProductsService;
  private final GetProductService getProductService;

  public ProductController(
      ListProductsService listProductsService, GetProductService getProductService) {
    this.listProductsService = listProductsService;
    this.getProductService = getProductService;
  }

  @GetMapping
  public List<ProductResponse> list() {
    return listProductsService.handle().stream().map(ProductResponse::from).toList();
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductResponse> get(@PathVariable String productId) {
    return getProductService
        .handle(productId)
        .map(ProductResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
