package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public record SpeedBroadcastEvent(
    long tickIntervalMs,
    long chaosCheckIntervalMs,
    String intensityLevel,
    double volatilityMultiplier,
    long aiEventIntervalMs,
    OffsetDateTime occurredAt)
    implements BroadcastEvent {

  public SpeedBroadcastEvent(
      long tickIntervalMs,
      long chaosCheckIntervalMs,
      String intensityLevel,
      double volatilityMultiplier,
      long aiEventIntervalMs) {
    this(
        tickIntervalMs,
        chaosCheckIntervalMs,
        intensityLevel,
        volatilityMultiplier,
        aiEventIntervalMs,
        OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.SPEED_CONFIG;
  }
}
