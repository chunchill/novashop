package com.novashop.commerce.catalog.infrastructure.seed;

import com.novashop.commerce.catalog.application.port.ProductRepository;
import com.novashop.commerce.catalog.domain.money.Money;
import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogSeedRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(CatalogSeedRunner.class);

  private final ProductRepository productRepository;

  public CatalogSeedRunner(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!productRepository.findAll().isEmpty()) {
      return;
    }
    log.info("Seeding catalog with demo products");
    productRepository.save(
        new Product(
            "prod-mug",
            "Nova Mug",
            "Ceramic mug with NovaShop mark",
            List.of(
                new Sku("sku-mug-std", "Standard mug", Money.cny("49.00")),
                new Sku("sku-mug-xl", "XL mug", Money.cny("69.00")))));
    productRepository.save(
        new Product(
            "prod-tee",
            "Nova Tee",
            "Soft cotton tee",
            List.of(
                new Sku("sku-tee-m", "Tee M", Money.cny("129.00")),
                new Sku("sku-tee-l", "Tee L", Money.cny("129.00")))));
    productRepository.save(
        new Product(
            "prod-sticker",
            "Sticker Pack",
            "Assorted stickers",
            List.of(new Sku("sku-sticker", "Sticker pack", Money.cny("19.00")))));
  }
}
