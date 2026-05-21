package dev.pollito.stonks_java.chaos.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ChaosEvent(
    String headline,
    String symbol,
    BigDecimal impactPercent,
    String explanation,
    List<String> affectedSymbols,
    String sourceHeadline,
    OffsetDateTime occurredAt,
    ChaosEventType type,
    ChaosEventSeverity severity) {}
