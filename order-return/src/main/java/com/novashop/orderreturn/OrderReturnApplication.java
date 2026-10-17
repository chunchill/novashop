package com.novashop.orderreturn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrderReturnApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderReturnApplication.class, args);
  }
}
