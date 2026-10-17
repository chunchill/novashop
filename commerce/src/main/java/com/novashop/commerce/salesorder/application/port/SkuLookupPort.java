package com.novashop.commerce.salesorder.application.port;

import com.novashop.commerce.salesorder.domain.money.Money;
import java.util.Optional;

/** Anti-corruption / local ACL into catalog for pricing at order time. */
public interface SkuLookupPort {

  Optional<SkuQuote> findBySkuId(String skuId);

  record SkuQuote(String skuId, String name, Money unitPrice) {}
}
