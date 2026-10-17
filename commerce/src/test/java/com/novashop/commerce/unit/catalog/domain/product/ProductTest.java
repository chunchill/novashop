package com.novashop.commerce.unit.catalog.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.commerce.catalog.domain.money.Money;
import com.novashop.commerce.catalog.domain.product.Product;
import com.novashop.commerce.catalog.domain.product.Sku;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  void findSku_returnsMatch() {
    var product =
        new Product(
            "p1",
            "Mug",
            "desc",
            List.of(new Sku("sku-1", "Std", Money.cny("10.00"))));

    assertThat(product.findSku("sku-1")).isPresent();
    assertThat(product.findSku("missing")).isEmpty();
  }

  @Test
  void rejectsEmptySkus() {
    assertThatThrownBy(() -> new Product("p1", "Mug", "desc", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
