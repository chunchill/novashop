package com.novashop.commerce.salesorder.application.port;

public interface OutboxPort {

  void append(Object event);
}
