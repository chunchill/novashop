package com.novashop.paymentrefund.application.port;

public interface RefundTerminalOutboxPort {

  void append(Object event);
}
