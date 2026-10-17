package com.novashop.fulfillment.unit.domain.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novashop.fulfillment.domain.inventory.InventoryException;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import org.junit.jupiter.api.Test;

class InventoryPositionTest {

  @Test
  void reserve_reducesAvailable() {
    InventoryPosition pos = InventoryPosition.seed("sku-1", 10);
    pos.reserve(3);
    assertThat(pos.reserved()).isEqualTo(3);
    assertThat(pos.available()).isEqualTo(7);
  }

  @Test
  void reserve_rejectsInsufficient() {
    InventoryPosition pos = InventoryPosition.seed("sku-1", 2);
    assertThatThrownBy(() -> pos.reserve(3))
        .isInstanceOf(InventoryException.class)
        .hasMessageContaining("insufficient");
  }
}
