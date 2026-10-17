package com.novashop.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommerceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CommerceApplication.class, args);
  }
}
