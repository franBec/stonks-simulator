package dev.pollito.stonks_java.trade.application.service;

import static dev.pollito.stonks_java.trade.domain.TradeAction.BUY;
import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutionPortOut;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOut;
import dev.pollito.stonks_java.trade.application.port.out.TradePortfolioStatePortOut;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutedEvent;
import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import dev.pollito.stonks_java.trade.domain.TradePortfolioState;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService implements TradePortIn {

  private static final long PORTFOLIO_ID = 1L;

  private final TradeExecutionPortOut tradeExecutionPortOut;
  private final TradeHistoryPortOut tradeHistoryPortOut;
  private final TradePortfolioStatePortOut tradePortfolioStatePortOut;
  private final StockPortIn stockPortIn;
  private final ApplicationEventPublisher events;

  @Override
  @Transactional
  public TradeExecutionResult executeTrade(Trade trade) {
    TradePortfolioState state = tradePortfolioStatePortOut.getState(PORTFOLIO_ID, trade.symbol());
    TradeExecutionResult result =
        tradeExecutionPortOut.executeTrade(
            new TradeExecutionInput(
                trade.action(),
                trade.symbol(),
                trade.quantity(),
                stockPortIn.getStocks().stream()
                    .filter(s -> s.symbol().equals(trade.symbol()))
                    .findFirst()
                    .map(s -> s.price().doubleValue())
                    .orElse(0.0),
                state.cashBalance(),
                state.holdingQty()));

    if (result.status() == ACCEPTED) {
      double newCostBasis;
      if (trade.action() == BUY) {
        newCostBasis = state.costBasis() + result.totalCost();
      } else {
        newCostBasis =
            state.holdingQty() > 0
                ? state.costBasis() * result.newQuantity() / state.holdingQty()
                : 0;
      }
      tradePortfolioStatePortOut.applyExecution(
          PORTFOLIO_ID,
          trade.symbol(),
          BigDecimal.valueOf(result.newCashBalance()),
          result.newQuantity(),
          BigDecimal.valueOf(newCostBasis));
      tradeHistoryPortOut.recordExecution(trade, result, PORTFOLIO_ID);
      events.publishEvent(
          new TradeExecutedEvent(trade.action(), result, trade.symbol(), trade.quantity()));
    }

    return result;
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryPortOut.getTradeHistory(pageable);
  }
}
