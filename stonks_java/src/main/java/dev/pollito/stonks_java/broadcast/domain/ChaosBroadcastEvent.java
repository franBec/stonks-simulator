package dev.pollito.stonks_java.broadcast.domain;

import java.time.OffsetDateTime;

public record ChaosBroadcastEvent(
    String headline, String symbol, double impact, String explanation, OffsetDateTime occurredAt)
    implements BroadcastEvent {

  public ChaosBroadcastEvent(String headline, String symbol, double impact, String explanation) {
    this(headline, symbol, impact, explanation, OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.CHAOS_EVENT;
  }
}
