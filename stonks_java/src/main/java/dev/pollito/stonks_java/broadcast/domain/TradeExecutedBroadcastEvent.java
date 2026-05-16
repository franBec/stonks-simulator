package dev.pollito.stonks_java.broadcast.domain;

import dev.pollito.stonks_java.trade.domain.TradeAction;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import java.time.OffsetDateTime;

public record TradeExecutedBroadcastEvent(
    TradeAction action,
    TradeExecutionResult result,
    String symbol,
    int quantity,
    OffsetDateTime occurredAt)
    implements BroadcastEvent {

  public TradeExecutedBroadcastEvent(
      TradeAction action, TradeExecutionResult result, String symbol, int quantity) {
    this(action, result, symbol, quantity, OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.TRADE_EXECUTED;
  }
}
