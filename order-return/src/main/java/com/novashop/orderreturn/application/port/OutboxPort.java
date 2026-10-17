package com.novashop.orderreturn.application.port;

public interface OutboxPort {

  void append(Object event);
}
