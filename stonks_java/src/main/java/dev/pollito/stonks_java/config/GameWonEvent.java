package dev.pollito.stonks_java.config;

import java.time.OffsetDateTime;

public record GameWonEvent(OffsetDateTime occurredAt) {

  public GameWonEvent() {
    this(OffsetDateTime.now());
  }
}
