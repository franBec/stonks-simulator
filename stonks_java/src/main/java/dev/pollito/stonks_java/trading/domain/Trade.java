package dev.pollito.stonks_java.trading.domain;

public record Trade(
    TradeAction action, String symbol, int quantity, double price, double cashBalance) {}
