package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public record GameResetBroadcastEvent(OffsetDateTime occurredAt) implements BroadcastEvent {

  public GameResetBroadcastEvent() {
    this(OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.GAME_RESET;
  }
}
