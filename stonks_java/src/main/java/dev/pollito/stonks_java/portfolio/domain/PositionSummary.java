package dev.pollito.stonks_java.portfolio.domain;

import java.math.BigDecimal;

public record PositionSummary(
    String symbol,
    int quantity,
    BigDecimal currentPrice,
    BigDecimal marketValue,
    BigDecimal costBasis,
    BigDecimal unrealizedPnl) {}
