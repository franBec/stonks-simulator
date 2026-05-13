package dev.pollito.stonks_java.trade.adapter.out.cobol.dto;

public record CobolTradeValidationRequest(
    String action, String symbol, int quantity, double price, double cashBalance) {}
