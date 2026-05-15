package dev.pollito.stonks_java.trade.application.service;

import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutionPortOut;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOut;
import dev.pollito.stonks_java.trade.application.port.out.TradePortfolioStatePortOut;
import dev.pollito.stonks_java.trade.application.port.out.TradeValidationPortOut;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import dev.pollito.stonks_java.trade.domain.TradePortfolioState;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService implements TradePortIn {

  private static final long PORTFOLIO_ID = 1L;

  private final TradeValidationPortOut tradeValidationPortOut;
  private final TradeExecutionPortOut tradeExecutionPortOut;
  private final TradeHistoryPortOut tradeHistoryPortOut;
  private final TradePortfolioStatePortOut tradePortfolioStatePortOut;
  private final StockPortIn stockPortIn;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradeValidationPortOut.validateTrade(trade);
  }

  @Override
  @Transactional
  public TradeExecutionResult executeTrade(Trade trade) {
    double currentPrice =
        stockPortIn.getStocks().stream()
            .filter(s -> s.symbol().equals(trade.symbol()))
            .findFirst()
            .map(s -> s.price().doubleValue())
            .orElse(0.0);

    TradePortfolioState state =
        tradePortfolioStatePortOut.getState(PORTFOLIO_ID, trade.symbol());

    TradeExecutionInput input =
        new TradeExecutionInput(
            trade.action(),
            trade.symbol(),
            trade.quantity(),
            currentPrice,
            state.cashBalance(),
            state.holdingQty());

    TradeExecutionResult result = tradeExecutionPortOut.executeTrade(input);

    if (result.status() == ACCEPTED) {
      tradePortfolioStatePortOut.applyExecution(
          PORTFOLIO_ID,
          trade.symbol(),
          BigDecimal.valueOf(result.newCashBalance()),
          result.newQuantity());
      tradeHistoryPortOut.recordExecution(trade, result, PORTFOLIO_ID);
    }

    return result;
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryPortOut.getTradeHistory(pageable);
  }
}
