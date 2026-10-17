package com.novashop.paymentrefund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentRefundApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentRefundApplication.class, args);
  }
}
