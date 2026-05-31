package dev.pollito.stonks_java.trade.application.port.in;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradePortIn {
  TradeExecutionResult executeTrade(Trade trade);

  Page<TradeHistoryItem> getTradeHistory(Pageable pageable);
}
