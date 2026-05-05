package dev.pollito.stonks_java.trading.adapter.out.cobol.dto;

public record CobolTradeValidationRequest(
    String action, String symbol, int quantity, double price, double cashBalance) {}
