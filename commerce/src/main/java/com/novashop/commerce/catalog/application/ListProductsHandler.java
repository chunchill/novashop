package com.novashop.commerce.catalog.application;

import com.novashop.commerce.catalog.application.port.ProductRepository;
import com.novashop.commerce.catalog.domain.product.Product;
import java.util.List;
import java.util.Objects;

public class ListProductsHandler {

  private final ProductRepository repository;

  public ListProductsHandler(ProductRepository repository) {
    this.repository = Objects.requireNonNull(repository, "repository");
  }

  public List<Product> handle() {
    return repository.findAll();
  }
}
