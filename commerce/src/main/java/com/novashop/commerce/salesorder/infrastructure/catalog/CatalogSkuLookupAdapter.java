package com.novashop.commerce.salesorder.infrastructure.catalog;

import com.novashop.commerce.catalog.application.port.ProductRepository;
import com.novashop.commerce.salesorder.application.port.SkuLookupPort;
import com.novashop.commerce.salesorder.domain.money.Money;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * In-process ACL from salesorder → catalog (same deployable, separate packages). Teaching note:
 * cross-BC HTTP ACL is deferred; demo-web reads order JSON then posts seed fields to order-return.
 */
@Component
public class CatalogSkuLookupAdapter implements SkuLookupPort {

  private final ProductRepository productRepository;

  public CatalogSkuLookupAdapter(ProductRepository productRepository) {
    this.productRepository = Objects.requireNonNull(productRepository, "productRepository");
  }

  @Override
  public Optional<SkuQuote> findBySkuId(String skuId) {
    return productRepository
        .findSkuById(skuId)
        .map(
            sku ->
                new SkuQuote(
                    sku.skuId(),
                    sku.name(),
                    Money.of(sku.unitPrice().amount(), sku.unitPrice().currency())));
  }
}
