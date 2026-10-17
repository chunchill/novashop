package com.novashop.fulfillment.application.port;

import com.novashop.fulfillment.domain.fulfillment.FulfillmentOrder;
import java.util.Optional;

public interface FulfillmentOrderRepository {

  Optional<FulfillmentOrder> findByOrderId(String orderId);

  void save(FulfillmentOrder order);
}
