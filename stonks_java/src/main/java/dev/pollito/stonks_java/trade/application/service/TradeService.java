package dev.pollito.stonks_java.trade.application.service;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutorPortOutCobol;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOutJpa;
import dev.pollito.stonks_java.trade.application.port.out.TradeValidatorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService implements TradePortIn {
  private final TradeValidatorPortOutCobol tradeValidatorPortOutCobol;
  private final TradeExecutorPortOutCobol tradeExecutorPortOutCobol;
  private final TradeHistoryPortOutJpa tradeHistoryPortOutJpa;
  private final StockPortIn stockPortIn;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradeValidatorPortOutCobol.validateTrade(trade);
  }

  @Override
  public TradeExecutionResult executeTrade(Trade trade) {
    double currentPrice =
        stockPortIn.getStocks().stream()
            .filter(s -> s.symbol().equals(trade.symbol()))
            .findFirst()
            .map(s -> s.price().doubleValue())
            .orElse(0.0);
    Trade enriched =
        new Trade(
            trade.action(), trade.symbol(), trade.quantity(), currentPrice, trade.cashBalance());
    return tradeExecutorPortOutCobol.executeTrade(enriched);
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryPortOutJpa.getTradeHistory(pageable);
  }
}
