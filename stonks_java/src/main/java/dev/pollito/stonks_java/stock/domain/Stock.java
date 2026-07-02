package dev.pollito.stonks_java.stock.domain;

import java.math.BigDecimal;

public record Stock(
    String symbol,
    String name,
    String description,
    BigDecimal basePrice,
    BigDecimal volatility,
    Trend trend) {}
