package dev.pollito.stonks_java.trade.adapter.out.cobol.dto;

public record CobolTradeValidationResult(
    String status, String errorCode, String message, double totalCost, double remainingCash) {}
