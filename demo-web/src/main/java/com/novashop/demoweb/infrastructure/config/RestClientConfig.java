package com.novashop.demoweb.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  RestClient commerceRestClient(@Value("${novashop.services.commerce-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  RestClient orderReturnRestClient(@Value("${novashop.services.order-return-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  RestClient paymentRefundRestClient(
      @Value("${novashop.services.payment-refund-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  RestClient fulfillmentRestClient(
      @Value("${novashop.services.fulfillment-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }
}
