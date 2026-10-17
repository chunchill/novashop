package com.novashop.fulfillment.infrastructure.config;

import com.novashop.fulfillment.application.GetFulfillmentByOrderHandler;
import com.novashop.fulfillment.application.GetInventoryHandler;
import com.novashop.fulfillment.application.ReserveStockFromOrderPlacedHandler;
import com.novashop.fulfillment.application.port.FulfillmentOrderRepository;
import com.novashop.fulfillment.application.port.InventoryRepository;
import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import com.novashop.platform.events.OrderPlaced;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ApplicationHandlersConfig {

  @Bean
  ReserveStockFromOrderPlacedHandler reserveStockFromOrderPlacedHandler(
      InventoryRepository inventoryRepository,
      FulfillmentOrderRepository fulfillmentOrderRepository,
      @Value("${novashop.fulfillment.warehouse-code}") String warehouseCode) {
    return new ReserveStockFromOrderPlacedHandler(
        inventoryRepository, fulfillmentOrderRepository, warehouseCode);
  }

  @Bean
  GetInventoryHandler getInventoryHandler(InventoryRepository inventoryRepository) {
    return new GetInventoryHandler(inventoryRepository);
  }

  @Bean
  GetFulfillmentByOrderHandler getFulfillmentByOrderHandler(
      FulfillmentOrderRepository fulfillmentOrderRepository) {
    return new GetFulfillmentByOrderHandler(fulfillmentOrderRepository);
  }

  @Service
  public static class ReserveStockService {

    private final ReserveStockFromOrderPlacedHandler handler;

    public ReserveStockService(ReserveStockFromOrderPlacedHandler handler) {
      this.handler = handler;
    }

    @Transactional
    public FulfillmentOrder handle(OrderPlaced event) {
      return handler.handle(event);
    }
  }

  @Service
  public static class GetInventoryService {

    private final GetInventoryHandler handler;

    public GetInventoryService(GetInventoryHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public Optional<InventoryPosition> handle(String skuId) {
      return handler.handle(skuId);
    }
  }

  @Service
  public static class GetFulfillmentByOrderService {

    private final GetFulfillmentByOrderHandler handler;

    public GetFulfillmentByOrderService(GetFulfillmentByOrderHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public Optional<FulfillmentOrder> handle(String orderId) {
      return handler.handle(orderId);
    }
  }
}
