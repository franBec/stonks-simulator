package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradePortOutJpa {
  Page<TradeHistoryItem> getTradeHistory(Pageable pageable);
}
