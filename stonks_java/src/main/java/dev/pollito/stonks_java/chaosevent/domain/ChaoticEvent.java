package dev.pollito.stonks_java.chaosevent.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ChaoticEvent(
    String headline,
    String symbol,
    BigDecimal impactPercent,
    String explanation,
    List<String> affectedSymbols,
    String sourceHeadline,
    OffsetDateTime occurredAt,
    ChaoticEventType type,
    ChaoticEventSeverity severity,
    String sourceUrl) {}
