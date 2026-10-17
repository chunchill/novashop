package com.novashop.commerce.catalog.application.port;

import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

  List<Product> findAll();

  Optional<Product> findById(String productId);

  Optional<Sku> findSkuById(String skuId);

  void save(Product product);
}
