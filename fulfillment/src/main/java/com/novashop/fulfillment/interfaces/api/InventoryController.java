package com.novashop.fulfillment.interfaces.api;

import com.novashop.fulfillment.infrastructure.config.ApplicationHandlersConfig.GetInventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

  private final GetInventoryService getInventoryService;

  public InventoryController(GetInventoryService getInventoryService) {
    this.getInventoryService = getInventoryService;
  }

  @GetMapping("/{skuId}")
  public ResponseEntity<InventoryResponse> get(@PathVariable String skuId) {
    return getInventoryService
        .handle(skuId)
        .map(InventoryResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
