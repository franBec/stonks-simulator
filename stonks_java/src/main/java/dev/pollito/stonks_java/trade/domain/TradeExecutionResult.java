package dev.pollito.stonks_java.trade.domain;

public record TradeExecutionResult(
    ValidationStatus status,
    String errorCode,
    String message,
    double newCashBalance,
    int newQuantity,
    double totalCost) {}
