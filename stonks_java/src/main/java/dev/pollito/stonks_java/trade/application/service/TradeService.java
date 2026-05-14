package dev.pollito.stonks_java.trade.application.service;

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

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradeValidatorPortOutCobol.validateTrade(trade);
  }

  @Override
  public TradeExecutionResult executeTrade(Trade trade) {
    return tradeExecutorPortOutCobol.executeTrade(trade);
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryPortOutJpa.getTradeHistory(pageable);
  }
}
