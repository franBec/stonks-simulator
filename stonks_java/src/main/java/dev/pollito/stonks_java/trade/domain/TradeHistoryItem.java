package dev.pollito.stonks_java.trade.domain;

import java.time.OffsetDateTime;

public record TradeHistoryItem(
    long id,
    String action,
    String symbol,
    int quantity,
    double price,
    double totalCost,
    double cashBalanceAfter,
    OffsetDateTime executedAt) {}
