package dev.pollito.stonks_java.trade.application.port.in;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradePortIn {
  TradeValidation validateTrade(Trade trade);

  TradeExecutionResult executeTrade(Trade trade);

  Page<TradeHistoryItem> getTradeHistory(Pageable pageable);
}
