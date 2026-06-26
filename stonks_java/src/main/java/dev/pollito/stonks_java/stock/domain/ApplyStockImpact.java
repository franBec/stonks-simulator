package dev.pollito.stonks_java.stock.domain;

import java.math.BigDecimal;

public record ApplyStockImpact(String symbol, BigDecimal impactPercent) {}
