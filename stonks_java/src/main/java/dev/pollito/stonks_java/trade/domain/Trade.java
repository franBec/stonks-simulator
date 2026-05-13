package dev.pollito.stonks_java.trade.domain;

public record Trade(
    TradeAction action, String symbol, int quantity, double price, double cashBalance) {}
