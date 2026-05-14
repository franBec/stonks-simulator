package dev.pollito.stonks_java.trade.adapter.out.cobol.dto;

public record CobolPortfolioMgrRequest(
    String action, String symbol, int quantity, double price, double cashBalance, int holdingQty) {}
