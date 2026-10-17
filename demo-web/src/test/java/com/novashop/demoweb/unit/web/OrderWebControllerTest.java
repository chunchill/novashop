package com.novashop.demoweb.unit.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.novashop.demoweb.infrastructure.client.CommerceClient;
import com.novashop.demoweb.infrastructure.client.CommerceClient.OrderDto;
import com.novashop.demoweb.infrastructure.client.FulfillmentClient;
import com.novashop.demoweb.web.OrderWebController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

@ExtendWith(MockitoExtension.class)
class OrderWebControllerTest {

  @Mock CommerceClient commerceClient;
  @Mock FulfillmentClient fulfillmentClient;
  @InjectMocks OrderWebController controller;

  @Test
  void placeOrder_redirectsToDetail() {
    when(commerceClient.createOrder(any()))
        .thenReturn(
            new OrderDto("ord-1", "pay-1", "49.00", "49.00", "CNY", List.of()));

    RedirectAttributes attrs = new RedirectAttributesModelMap();
    String view = controller.placeOrder(Map.of("qty_sku-mug-std", "1"), attrs);

    assertThat(view).isEqualTo("redirect:/orders/ord-1");
  }

  @Test
  void placeOrder_emptySelection_redirectsBack() {
    RedirectAttributes attrs = new RedirectAttributesModelMap();
    String view = controller.placeOrder(Map.of("qty_sku-mug-std", "0"), attrs);

    assertThat(view).isEqualTo("redirect:/orders/new");
    assertThat(attrs.getFlashAttributes()).containsKey("error");
  }
}
