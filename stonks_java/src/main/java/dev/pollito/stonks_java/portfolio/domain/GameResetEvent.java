package dev.pollito.stonks_java.portfolio.domain;

import java.time.OffsetDateTime;

public record GameResetEvent(OffsetDateTime occurredAt) {

  public GameResetEvent() {
    this(OffsetDateTime.now());
  }
}
