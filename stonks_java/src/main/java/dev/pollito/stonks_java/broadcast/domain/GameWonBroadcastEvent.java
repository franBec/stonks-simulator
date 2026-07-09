package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public record GameWonBroadcastEvent(OffsetDateTime occurredAt) implements BroadcastEvent {

  public GameWonBroadcastEvent() {
    this(OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.GAME_WON;
  }
}
