package dev.pollito.stonks_java.trade.domain;

public record TradePortfolioState(double cashBalance, int holdingQty, double costBasis) {}
