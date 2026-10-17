package com.novashop.commerce.catalog.interfaces.api;

import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import java.util.List;

public record ProductResponse(
    String productId, String name, String description, List<SkuResponse> skus) {

  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.productId(),
        product.name(),
        product.description(),
        product.skus().stream().map(SkuResponse::from).toList());
  }

  public record SkuResponse(String skuId, String name, String unitPrice, String currency) {
    static SkuResponse from(Sku sku) {
      return new SkuResponse(
          sku.skuId(),
          sku.name(),
          sku.unitPrice().amount().toPlainString(),
          sku.unitPrice().currency());
    }
  }
}
