package dev.pollito.stonks_java.trade.domain;

import java.time.OffsetDateTime;

public record TradeExecutedEvent(
    TradeAction action,
    TradeExecutionResult result,
    String symbol,
    int quantity,
    OffsetDateTime occurredAt) {

  public TradeExecutedEvent(
      TradeAction action, TradeExecutionResult result, String symbol, int quantity) {
    this(action, result, symbol, quantity, OffsetDateTime.now());
  }
}
