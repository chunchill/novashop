package com.novashop.paymentrefund.unit.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.novashop.paymentrefund.application.port.PspRefundClient;
import com.novashop.paymentrefund.infrastructure.messaging.ReturnApprovedListener;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Enforces Task 8 rule: listener must not inject PspRefundClient. */
class ReturnApprovedListenerIsolationTest {

  @Test
  void listener_constructors_do_not_take_psp_client() {
    for (Constructor<?> ctor : ReturnApprovedListener.class.getDeclaredConstructors()) {
      assertThat(Arrays.asList(ctor.getParameterTypes()))
          .as("ReturnApprovedListener must not depend on PspRefundClient")
          .doesNotContain(PspRefundClient.class);
    }
  }
}
