package dev.pollito.stonks_java.trade.domain;

public record TradeExecutionInput(
    TradeAction action,
    String symbol,
    int quantity,
    double price,
    double cashBalance,
    int holdingQty) {}
