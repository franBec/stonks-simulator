package dev.pollito.stonks_java.trading.adapter.out.cobol.dto;

public record CobolTradeValidationResult(
    String status, String errorCode, String message, double totalCost, double remainingCash) {}
