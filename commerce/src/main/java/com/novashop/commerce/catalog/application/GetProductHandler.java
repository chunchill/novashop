package com.novashop.commerce.catalog.application;

import com.novashop.commerce.catalog.application.port.ProductRepository;
import com.novashop.commerce.catalog.domain.product.Product;
import java.util.Objects;
import java.util.Optional;

public class GetProductHandler {

  private final ProductRepository repository;

  public GetProductHandler(ProductRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public Optional<Product> handle(String productId) {
    Objects.requireNonNull(productId, "productId");
    return repository.findById(productId);
  }
}
