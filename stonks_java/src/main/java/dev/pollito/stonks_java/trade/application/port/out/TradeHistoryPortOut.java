package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeHistoryPortOut {
  Page<TradeHistoryItem> getTradeHistory(Pageable pageable);
  void recordExecution(Trade trade, TradeExecutionResult result, long portfolioId);
}
