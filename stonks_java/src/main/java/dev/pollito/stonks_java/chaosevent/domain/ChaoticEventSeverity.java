package dev.pollito.stonks_java.chaosevent.domain;

import dev.pollito.stonks_java.util.enums.ValuedEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChaoticEventSeverity implements ValuedEnum<String> {
  LOW("LOW"),
  MEDIUM("MEDIUM"),
  HIGH("HIGH"),
  CRITICAL("CRITICAL");

  private final String value;
}
