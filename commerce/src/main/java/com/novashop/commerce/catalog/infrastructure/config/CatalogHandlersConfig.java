package com.novashop.commerce.catalog.infrastructure.config;

import com.novashop.commerce.catalog.application.GetProductHandler;
import com.novashop.commerce.catalog.application.ListProductsHandler;
import com.novashop.commerce.catalog.application.port.ProductRepository;
import com.novashop.commerce.catalog.domain.product.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class CatalogHandlersConfig {

  @Bean
  ListProductsHandler listProductsHandler(ProductRepository repository) {
    return new ListProductsHandler(repository);
  }

  @Bean
  GetProductHandler getProductHandler(ProductRepository repository) {
    return new GetProductHandler(repository);
  }

  @Service
  public static class ListProductsService {

    private final ListProductsHandler handler;

    public ListProductsService(ListProductsHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public List<Product> handle() {
      return handler.handle();
    }
  }

  @Service
  public static class GetProductService {

    private final GetProductHandler handler;

    public GetProductService(GetProductHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public Optional<Product> handle(String productId) {
      return handler.handle(productId);
    }
  }
}
