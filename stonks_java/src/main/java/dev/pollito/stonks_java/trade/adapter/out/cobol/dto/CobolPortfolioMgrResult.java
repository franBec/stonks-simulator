package dev.pollito.stonks_java.trade.adapter.out.cobol.dto;

public record CobolPortfolioMgrResult(
    String status,
    String errorCode,
    String message,
    double newCashBalance,
    int newQuantity,
    double totalCost) {}
