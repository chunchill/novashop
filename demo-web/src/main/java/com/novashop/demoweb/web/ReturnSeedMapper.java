package com.novashop.demoweb.web;

import com.novashop.demoweb.infrastructure.client.CommerceClient.OrderDto;
import com.novashop.demoweb.infrastructure.client.OrderReturnClient.CreateReturnDto;
import com.novashop.demoweb.infrastructure.client.OrderReturnClient.ReturnLineDto;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Pure mapping: commerce order JSON → order-return create-return <em>seed snapshot</em> fields.
 *
 * <p>This is a <strong>teaching transition</strong>, not the long-term ACL: demo-web (BFF) reads
 * commerce and POSTs snapshots into order-return so the return BC stays deployable without calling
 * commerce. A future step may move a read-only {@code OrderSnapshotClient} into order-return
 * infrastructure instead.
 */
public final class ReturnSeedMapper {

  private ReturnSeedMapper() {}

  public static CreateReturnDto fromOrder(String returnId, OrderDto order) {
    Objects.requireNonNull(returnId, "returnId");
    Objects.requireNonNull(order, "order");
    List<ReturnLineDto> lines =
        order.lines().stream()
            .map(l -> new ReturnLineDto(l.lineId(), l.quantity()))
            .toList();
    return new CreateReturnDto(
        returnId, order.paymentId(), order.refundableTotal(), lines, Set.of());
  }
}
