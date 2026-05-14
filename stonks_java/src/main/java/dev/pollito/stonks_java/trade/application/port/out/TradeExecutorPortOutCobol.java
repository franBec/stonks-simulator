package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;

public interface TradeExecutorPortOutCobol {
  TradeExecutionResult executeTrade(Trade trade);
}
