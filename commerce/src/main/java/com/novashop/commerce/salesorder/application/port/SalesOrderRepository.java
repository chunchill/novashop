package com.novashop.commerce.salesorder.application.port;

import com.novashop.commerce.salesorder.domain.order.SalesOrder;
import java.util.Optional;

public interface SalesOrderRepository {

  void save(SalesOrder order);

  Optional<SalesOrder> findById(String orderId);
}
