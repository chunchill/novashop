package com.novashop.fulfillment.infrastructure.seed;

import com.novashop.fulfillment.application.port.InventoryRepository;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds inventory for the same demo SKUs as commerce catalog. */
@Component
public class InventorySeedRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(InventorySeedRunner.class);

  private static final List<String> DEMO_SKUS =
      List.of("sku-mug-std", "sku-mug-xl", "sku-tee-m", "sku-tee-l", "sku-sticker");

  private final InventoryRepository inventoryRepository;

  public InventorySeedRunner(InventoryRepository inventoryRepository) {
    this.inventoryRepository = inventoryRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    for (String skuId : DEMO_SKUS) {
      if (inventoryRepository.findBySkuId(skuId).isEmpty()) {
        inventoryRepository.save(InventoryPosition.seed(skuId, 100));
        log.info("Seeded inventory sku={} onHand=100", skuId);
      }
    }
  }
}
