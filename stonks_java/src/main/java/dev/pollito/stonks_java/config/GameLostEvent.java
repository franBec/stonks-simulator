package dev.pollito.stonks_java.config;

import java.time.OffsetDateTime;

public record GameLostEvent(OffsetDateTime occurredAt) {

  public GameLostEvent() {
    this(OffsetDateTime.now());
  }
}
