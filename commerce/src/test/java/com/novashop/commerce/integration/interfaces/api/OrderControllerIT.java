package com.novashop.commerce.integration.interfaces.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novashop.commerce.salesorder.domain.money.Money;
import com.novashop.commerce.salesorder.domain.order.OrderLine;
import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import com.novashop.commerce.salesorder.infrastructure.config.SalesOrderHandlersConfig.CreateOrderService;
import com.novashop.commerce.salesorder.infrastructure.config.SalesOrderHandlersConfig.GetOrderService;
import com.novashop.commerce.salesorder.interfaces.api.OrderController;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
@ActiveProfiles("test")
class OrderControllerIT {

  @Autowired MockMvc mockMvc;

  @MockBean CreateOrderService createOrderService;
  @MockBean GetOrderService getOrderService;

  @Test
  void create_returnsCreatedOrder() throws Exception {
    SalesOrder order =
        SalesOrder.reconstitute(
            "ord-1",
            "pay-1",
            List.of(new OrderLine("L1", "sku-mug-std", "Mug", 1, Money.cny("49.00"))),
            Money.cny("49.00"),
            Money.cny("49.00"));
    when(createOrderService.handle(any())).thenReturn(order);

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "orderId": "ord-1",
                      "lines": [{"skuId": "sku-mug-std", "quantity": 1}]
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").value("ord-1"))
        .andExpect(jsonPath("$.paymentId").value("pay-1"))
        .andExpect(jsonPath("$.lines[0].skuId").value("sku-mug-std"));
  }

  @Test
  void get_missingOrder_returns404() throws Exception {
    when(getOrderService.handle("missing")).thenReturn(Optional.empty());

    mockMvc.perform(get("/orders/missing")).andExpect(status().isNotFound());
  }
}
