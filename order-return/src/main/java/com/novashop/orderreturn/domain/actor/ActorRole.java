package com.novashop.orderreturn.domain.actor;

import java.util.Locale;
import java.util.Objects;

public enum ActorRole {
  AGENT,
  SUPERVISOR;

  public static ActorRole parse(String value) {
    Objects.requireNonNull(value, "role");
    return ActorRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }
}
