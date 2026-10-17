package com.novashop.demoweb.web;

import com.novashop.demoweb.infrastructure.client.CommerceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogController {

  private final CommerceClient commerceClient;

  public CatalogController(CommerceClient commerceClient) {
    this.commerceClient = commerceClient;
  }

  @GetMapping("/")
  public String catalog(Model model) {
    model.addAttribute("products", commerceClient.listProducts());
    return "catalog";
  }
}
