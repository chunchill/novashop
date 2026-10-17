package com.novashop.orderreturn.application.port;

import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.orderreturn.domain.returnrequest.ReturnStatus;
import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository {

  Optional<ReturnRequest> findById(String id);

  void save(ReturnRequest returnRequest);

  List<ReturnRequest> findByStatus(ReturnStatus status);
}
