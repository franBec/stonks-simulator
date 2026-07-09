package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public record GameLostBroadcastEvent(OffsetDateTime occurredAt) implements BroadcastEvent {

  public GameLostBroadcastEvent() {
    this(OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.GAME_LOST;
  }
}
