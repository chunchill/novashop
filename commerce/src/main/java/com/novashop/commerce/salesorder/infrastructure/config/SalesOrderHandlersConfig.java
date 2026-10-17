package com.novashop.commerce.salesorder.infrastructure.config;

import com.novashop.commerce.salesorder.application.CreateOrderCommand;
import com.novashop.commerce.salesorder.application.CreateOrderHandler;
import com.novashop.commerce.salesorder.application.GetOrderHandler;
import com.novashop.commerce.salesorder.application.port.OutboxPort;
import com.novashop.commerce.salesorder.application.port.SalesOrderRepository;
import com.novashop.commerce.salesorder.application.port.SkuLookupPort;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SalesOrderHandlersConfig {

  @Bean
  CreateOrderHandler createOrderHandler(
      SalesOrderRepository orderRepository, SkuLookupPort skuLookup, OutboxPort outbox) {
    return new CreateOrderHandler(orderRepository, skuLookup, outbox);
  }

  @Bean
  GetOrderHandler getOrderHandler(SalesOrderRepository repository) {
    return new GetOrderHandler(repository);
  }

  @Service
  public static class CreateOrderService {

    private final CreateOrderHandler handler;

    public CreateOrderService(CreateOrderHandler handler) {
      this.handler = handler;
    }

    @Transactional
    public SalesOrder handle(CreateOrderCommand cmd) {
      return handler.handle(cmd);
    }
  }

  @Service
  public static class GetOrderService {

    private final GetOrderHandler handler;

    public GetOrderService(GetOrderHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public Optional<SalesOrder> handle(String orderId) {
      return handler.handle(orderId);
    }
  }
}
