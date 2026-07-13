package dev.pollito.stonks_java.broadcast.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record GameConfigBroadcastEvent(
    double winThreshold,
    double loseThreshold,
    BigDecimal initialCash,
    OffsetDateTime occurredAt)
    implements BroadcastEvent {

  public GameConfigBroadcastEvent(
      double winThreshold, double loseThreshold, BigDecimal initialCash) {
    this(winThreshold, loseThreshold, initialCash, OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.GAME_CONFIG;
  }
}
