package com.novashop.fulfillment.infrastructure.persistence;

import com.novashop.fulfillment.application.port.InventoryRepository;
import com.novashop.fulfillment.domain.inventory.InventoryPosition;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaInventoryRepository implements InventoryRepository {

  private final SpringDataInventoryJpaRepository jpa;

  public JpaInventoryRepository(SpringDataInventoryJpaRepository jpa) {
    this.jpa = Objects.requireNonNull(jpa, "jpa");
  }

  @Override
  public Optional<InventoryPosition> findBySkuId(String skuId) {
    return jpa.findById(skuId)
        .map(e -> InventoryPosition.reconstitute(e.getSkuId(), e.getOnHand(), e.getReserved()));
  }

  @Override
  public void save(InventoryPosition position) {
    Objects.requireNonNull(position, "position");
    InventoryPositionEntity entity =
        jpa.findById(position.skuId())
            .orElseGet(
                () ->
                    new InventoryPositionEntity(
                        position.skuId(), position.onHand(), position.reserved()));
    entity.setOnHand(position.onHand());
    entity.setReserved(position.reserved());
    jpa.save(entity);
  }
}
