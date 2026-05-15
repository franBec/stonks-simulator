package dev.pollito.stonks_java.trade.application.service;

import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.generated.entity.Portfolio;
import dev.pollito.stonks_java.generated.entity.Position;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPortfolioJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPositionJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeHistoryJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.mapper.TradeExecutionHistoryEntityMapper;
import dev.pollito.stonks_java.trade.application.port.in.TradePortIn;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutorPortOutCobol;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOutJpa;
import dev.pollito.stonks_java.trade.application.port.out.TradeValidatorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionInput;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import dev.pollito.stonks_java.trade.domain.TradeValidation;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeService implements TradePortIn {

  private static final long PORTFOLIO_ID = 1L;

  private final TradeValidatorPortOutCobol tradeValidatorPortOutCobol;
  private final TradeExecutorPortOutCobol tradeExecutorPortOutCobol;
  private final TradeHistoryPortOutJpa tradeHistoryPortOutJpa;
  private final StockPortIn stockPortIn;
  private final TradeExecutionPortfolioJpaRepository portfolioRepo;
  private final TradeExecutionPositionJpaRepository positionRepo;
  private final TradeHistoryJpaRepository tradeHistoryRepo;
  private final TradeExecutionHistoryEntityMapper historyEntityMapper;

  @Override
  public TradeValidation validateTrade(Trade trade) {
    return tradeValidatorPortOutCobol.validateTrade(trade);
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

    Portfolio portfolio = portfolioRepo.findById(PORTFOLIO_ID).orElseThrow();
    double cashBalance = portfolio.getCashBalance().doubleValue();

    Optional<Position> optPos =
        positionRepo.findByPortfolioIdAndSymbol(PORTFOLIO_ID, trade.symbol());
    int holdingQty = optPos.map(p -> p.getQuantity().intValue()).orElse(0);

    TradeExecutionInput input =
        new TradeExecutionInput(
            trade.action(),
            trade.symbol(),
            trade.quantity(),
            currentPrice,
            cashBalance,
            holdingQty);

    TradeExecutionResult result = tradeExecutorPortOutCobol.executeTrade(input);

    if (result.status() == ACCEPTED) {
      portfolio.setCashBalance(BigDecimal.valueOf(result.newCashBalance()));
      portfolioRepo.save(portfolio);

      Position position = optPos.orElseGet(Position::new);
      position.setPortfolio(portfolio);
      position.setSymbol(trade.symbol());
      position.setQuantity((long) result.newQuantity());
      positionRepo.save(position);

      tradeHistoryRepo.save(historyEntityMapper.map(trade, result, portfolio));
    }

    return result;
  }

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryPortOutJpa.getTradeHistory(pageable);
  }
}
