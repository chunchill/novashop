package com.novashop.orderreturn.interfaces.api;

public class InvalidActorHeaderException extends RuntimeException {

  public InvalidActorHeaderException(String headerName, String value) {
    super("invalid header " + headerName + ": " + value);
  }
}
