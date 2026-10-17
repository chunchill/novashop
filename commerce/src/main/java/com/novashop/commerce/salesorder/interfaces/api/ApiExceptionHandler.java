package com.novashop.commerce.salesorder.interfaces.api;

import com.novashop.commerce.salesorder.domain.order.SalesOrderException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(SalesOrderException.class)
  public ResponseEntity<Map<String, String>> handleSalesOrder(SalesOrderException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
  }
}
