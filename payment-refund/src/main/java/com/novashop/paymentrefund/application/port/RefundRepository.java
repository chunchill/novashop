package com.novashop.paymentrefund.application.port;

import com.novashop.paymentrefund.domain.refund.Refund;
import java.util.Optional;

public interface RefundRepository {

  Optional<Refund> findById(String id);

  void save(Refund refund);
}
