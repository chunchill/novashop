package com.novashop.orderreturn.interfaces.api;

import com.novashop.orderreturn.domain.actor.ActorRole;

public record ActorHeaders(String actorId, ActorRole role) {

  public static final String ACTOR_ID = "X-Actor-Id";
  public static final String ACTOR_ROLE = "X-Actor-Role";

  public static ActorHeaders require(String actorId, String actorRole) {
    if (actorId == null || actorId.isBlank()) {
      throw new MissingActorHeaderException(ACTOR_ID);
    }
    if (actorRole == null || actorRole.isBlank()) {
      throw new MissingActorHeaderException(ACTOR_ROLE);
    }
    try {
      return new ActorHeaders(actorId.trim(), ActorRole.parse(actorRole));
    } catch (IllegalArgumentException ex) {
      throw new InvalidActorHeaderException(ACTOR_ROLE, actorRole);
    }
  }
}
