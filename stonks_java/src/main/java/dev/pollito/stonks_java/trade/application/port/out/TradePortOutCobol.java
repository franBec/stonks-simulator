package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeValidation;

public interface TradePortOutCobol {
  TradeValidation validateTrade(Trade trade);

  TradeExecutionResult executeTrade(Trade trade);
}
