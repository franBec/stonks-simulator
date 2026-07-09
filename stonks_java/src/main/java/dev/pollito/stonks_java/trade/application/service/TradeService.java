package dev.pollito.stonks_java.trade.application.service;

import static dev.pollito.stonks_java.trade.domain.TradeAction.BUY;
import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.config.GameLostEvent;
import dev.pollito.stonks_java.config.GameWonEvent;
import dev.pollito.stonks_java.config.GameStateService;
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
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
  private final GameStateService gameStateService;

  @Value("${stonks.trade.fee-rate:0.005}")
  private double feeRate;

  @Value("${stonks.game.win-threshold:100000.00}")
  private double winThreshold;

  @Value("${stonks.game.lose-threshold:1000.00}")
  private double loseThreshold;

  @Override
  @Transactional
  public TradeExecutionResult executeTrade(Trade trade) {
    if (!gameStateService.isPlaying()) {
      return new TradeExecutionResult(
          ValidationStatus.REJECTED,
          "S999",
          "GAME OVER — reset to play again",
          tradePortfolioStatePortOut.getState(PORTFOLIO_ID, trade.symbol()).cashBalance(),
          tradePortfolioStatePortOut.getState(PORTFOLIO_ID, trade.symbol()).holdingQty(),
          0.0);
    }

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
                state.holdingQty(),
                feeRate));

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

      double currentPrice =
          stockPortIn.getStocks().stream()
              .filter(s -> s.symbol().equals(trade.symbol()))
              .findFirst()
              .map(s -> s.price().doubleValue())
              .orElse(0.0);
      double totalValue = result.newCashBalance() + result.newQuantity() * currentPrice;

      if (totalValue >= winThreshold) {
        gameStateService.markWon();
        events.publishEvent(new GameWonEvent());
      } else if (totalValue <= loseThreshold) {
        gameStateService.markLost();
        events.publishEvent(new GameLostEvent());
      }
    }

    return result;
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryPortOut.getTradeHistory(pageable);
  }
}
