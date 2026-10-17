package com.novashop.commerce.catalog.infrastructure.persistence;

import com.novashop.commerce.catalog.domain.money.Money;
import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import java.util.ArrayList;
import java.util.List;

final class ProductMapper {

  private ProductMapper() {}

  static Product toDomain(ProductEntity entity) {
    List<Sku> skus =
        entity.getSkus().stream()
            .map(
                s ->
                    new Sku(
                        s.getSkuId(),
                        s.getName(),
                        Money.of(s.getUnitPrice(), s.getCurrency())))
            .toList();
    return new Product(entity.getProductId(), entity.getName(), entity.getDescription(), skus);
  }

  static ProductEntity toEntity(Product product) {
    ProductEntity entity =
        new ProductEntity(product.productId(), product.name(), product.description());
    List<SkuEntity> skus = new ArrayList<>();
    for (Sku sku : product.skus()) {
      skus.add(
          new SkuEntity(
              sku.skuId(),
              sku.name(),
              sku.unitPrice().amount(),
              sku.unitPrice().currency(),
              entity));
    }
    entity.setSkus(skus);
    return entity;
  }

  static Sku toSku(SkuEntity entity) {
    return new Sku(
        entity.getSkuId(),
        entity.getName(),
        Money.of(entity.getUnitPrice(), entity.getCurrency()));
  }
}
