package dev.pollito.stonks_java.trade.domain;

public record TradeValidation(
    ValidationStatus status,
    String errorCode,
    String message,
    double totalCost,
    double remainingCash) {}
