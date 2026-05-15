package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;

public interface TradeExecutionPortOut {
  TradeExecutionResult executeTrade(TradeExecutionInput input);
}
