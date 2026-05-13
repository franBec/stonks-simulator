package dev.pollito.stonks_java.stock.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StockPrice(
    String symbol,
    String name,
    BigDecimal price,
    BigDecimal previousPrice,
    BigDecimal change,
    BigDecimal changePercent,
    OffsetDateTime timestamp) {}
