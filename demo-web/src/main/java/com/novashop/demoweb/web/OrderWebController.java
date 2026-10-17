package com.novashop.demoweb.web;

import com.novashop.demoweb.infrastructure.client.CommerceClient;
import com.novashop.demoweb.infrastructure.client.CommerceClient.CreateOrderDto;
import com.novashop.demoweb.infrastructure.client.CommerceClient.LineDto;
import com.novashop.demoweb.infrastructure.client.CommerceClient.OrderDto;
import com.novashop.demoweb.infrastructure.client.CommerceClient.SkuDto;
import com.novashop.demoweb.infrastructure.client.FulfillmentClient;
import com.novashop.demoweb.infrastructure.client.FulfillmentClient.InventoryDto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

  private final CommerceClient commerceClient;
  private final FulfillmentClient fulfillmentClient;

  public OrderWebController(CommerceClient commerceClient, FulfillmentClient fulfillmentClient) {
    this.commerceClient = commerceClient;
    this.fulfillmentClient = fulfillmentClient;
  }

  @GetMapping("/orders/new")
  public String newOrderForm(Model model) {
    model.addAttribute("products", commerceClient.listProducts());
    return "order-new";
  }

  @PostMapping("/orders")
  public String placeOrder(
      @RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
    List<LineDto> lines = new ArrayList<>();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (!entry.getKey().startsWith("qty_")) {
        continue;
      }
      String skuId = entry.getKey().substring("qty_".length());
      String raw = entry.getValue();
      if (raw == null || raw.isBlank()) {
        continue;
      }
      int qty = Integer.parseInt(raw);
      if (qty > 0) {
        lines.add(new LineDto(skuId, qty));
      }
    }
    if (lines.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Select at least one SKU with quantity > 0");
      return "redirect:/orders/new";
    }
    OrderDto order = commerceClient.createOrder(new CreateOrderDto(null, lines));
    return "redirect:/orders/" + order.orderId();
  }

  @GetMapping("/orders/{orderId}")
  public String orderDetail(@PathVariable String orderId, Model model) {
    OrderDto order = commerceClient.getOrder(orderId);
    model.addAttribute("order", order);
    model.addAttribute("fulfillment", fulfillmentClient.getByOrderId(orderId).orElse(null));
    Map<String, InventoryDto> inventoryBySku = new LinkedHashMap<>();
    for (var line : order.lines()) {
      fulfillmentClient.getInventory(line.skuId()).ifPresent(inv -> inventoryBySku.put(line.skuId(), inv));
    }
    model.addAttribute("inventoryBySku", inventoryBySku);
    return "order-detail";
  }

  /** Flatten SKUs for the place-order form. */
  public static List<SkuDto> flattenSkus(List<CommerceClient.ProductDto> products) {
    return products.stream().flatMap(p -> p.skus().stream()).toList();
  }
}
