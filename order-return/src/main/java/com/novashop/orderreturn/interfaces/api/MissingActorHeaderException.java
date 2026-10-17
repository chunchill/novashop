package com.novashop.orderreturn.interfaces.api;

public class MissingActorHeaderException extends RuntimeException {

  private final String headerName;

  public MissingActorHeaderException(String headerName) {
    super("missing required header: " + headerName);
    this.headerName = headerName;
  }

  public String headerName() {
    return headerName;
  }
}
