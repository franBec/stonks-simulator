package dev.pollito.stonks_java.trade.application.service;

import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.application.port.out.TradePortOutCobol;
import dev.pollito.stonks_java.trade.application.port.out.TradePortOutJpa;
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
  private final TradePortOutCobol tradePortOutCobol;
  private final TradePortOutJpa tradePortOutJpa;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradePortOutCobol.validateTrade(trade);
  }

  @Override
  public TradeExecutionResult executeTrade(Trade trade) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradePortOutJpa.getTradeHistory(pageable);
  }
}
