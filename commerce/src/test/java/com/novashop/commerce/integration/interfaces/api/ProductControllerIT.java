package com.novashop.commerce.integration.interfaces.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novashop.commerce.catalog.domain.money.Money;
import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import com.novashop.commerce.catalog.infrastructure.config.CatalogHandlersConfig.GetProductService;
import com.novashop.commerce.catalog.infrastructure.config.CatalogHandlersConfig.ListProductsService;
import com.novashop.commerce.catalog.interfaces.api.ProductController;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductController.class)
@ActiveProfiles("test")
class ProductControllerIT {

  @Autowired MockMvc mockMvc;

  @MockBean ListProductsService listProductsService;
  @MockBean GetProductService getProductService;

  @Test
  void list_returnsProducts() throws Exception {
    when(listProductsService.handle())
        .thenReturn(
            List.of(
                new Product(
                    "prod-mug",
                    "Nova Mug",
                    "desc",
                    List.of(new Sku("sku-mug-std", "Standard mug", Money.cny("49.00"))))));

    mockMvc
        .perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].productId").value("prod-mug"))
        .andExpect(jsonPath("$[0].skus[0].skuId").value("sku-mug-std"));
  }
}
