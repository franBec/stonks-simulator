package dev.pollito.stonks_java.stock.domain;

import java.math.BigDecimal;

public record Stock(
    String symbol, String name, BigDecimal basePrice, BigDecimal volatility, String trend) {}
