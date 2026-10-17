package com.novashop.demoweb.unit.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.demoweb.infrastructure.client.CommerceClient.OrderDto;
import com.novashop.demoweb.infrastructure.client.CommerceClient.OrderLineDto;
import com.novashop.demoweb.infrastructure.client.OrderReturnClient.CreateReturnDto;
import com.novashop.demoweb.web.ReturnSeedMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReturnSeedMapperTest {

  @Test
  void fromOrder_mapsPaymentIdRefundableAndLines() {
    OrderDto order =
        new OrderDto(
            "ord-1",
            "pay-1",
            "98.00",
            "98.00",
            "CNY",
            List.of(
                new OrderLineDto("L1", "sku-mug-std", "Mug", 2, "49.00"),
                new OrderLineDto("L2", "sku-tea", "Tea", 1, "20.00")));

    CreateReturnDto seed = ReturnSeedMapper.fromOrder("ret-1", order);

    assertThat(seed.id()).isEqualTo("ret-1");
    assertThat(seed.paymentId()).isEqualTo("pay-1");
    assertThat(seed.refundableTotal()).isEqualTo("98.00");
    assertThat(seed.alreadyApprovedLineIds()).isEmpty();
    assertThat(seed.lines()).hasSize(2);
    assertThat(seed.lines().get(0).orderLineId()).isEqualTo("L1");
    assertThat(seed.lines().get(0).remainingRefundableQty()).isEqualTo(2);
    assertThat(seed.lines().get(1).orderLineId()).isEqualTo("L2");
    assertThat(seed.lines().get(1).remainingRefundableQty()).isEqualTo(1);
  }
}
