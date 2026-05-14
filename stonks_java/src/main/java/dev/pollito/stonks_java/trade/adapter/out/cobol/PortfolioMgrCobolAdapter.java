package dev.pollito.stonks_java.trade.adapter.out.cobol;

import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.cobol.application.port.out.CobolAppPortOut;
import dev.pollito.stonks_java.generated.entity.Portfolio;
import dev.pollito.stonks_java.generated.entity.Position;
import dev.pollito.stonks_java.generated.entity.TradeHistory;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolPortfolioMgrRequest;
import dev.pollito.stonks_java.trade.adapter.out.cobol.dto.CobolPortfolioMgrResult;
import dev.pollito.stonks_java.trade.adapter.out.cobol.mapper.PortfolioMgrCobolMapper;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPortfolioJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPositionJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeHistoryJpaRepository;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"cobol", "production"})
@RequiredArgsConstructor
public class PortfolioMgrCobolAdapter implements TradeExecutorPortOutCobol {

  private static final String PROGRAM_NAME = "portfolio-mgr";
  private static final long PORTFOLIO_ID = 1L;

  private final CobolAppPortOut cobolApp;
  private final PortfolioMgrCobolMapper mapper;
  private final TradeExecutionPortfolioJpaRepository portfolioRepo;
  private final TradeExecutionPositionJpaRepository positionRepo;
  private final TradeHistoryJpaRepository tradeHistoryRepo;

  @Override
  @Transactional
  public TradeExecutionResult executeTrade(Trade trade) {
    Portfolio portfolio = portfolioRepo.findById(PORTFOLIO_ID).orElseThrow();
    double cashBalance = portfolio.getCashBalance().doubleValue();

    Optional<Position> optPos =
        positionRepo.findByPortfolioIdAndSymbol(PORTFOLIO_ID, trade.symbol());
    int holdingQty = optPos.map(p -> p.getQuantity().intValue()).orElse(0);

    CobolPortfolioMgrRequest req =
        new CobolPortfolioMgrRequest(
            trade.action().getValue(),
            trade.symbol(),
            trade.quantity(),
            trade.price(),
            cashBalance,
            holdingQty);

    CobolPortfolioMgrResult cobolResult =
        cobolApp.execute(PROGRAM_NAME, req, CobolPortfolioMgrResult.class);
    TradeExecutionResult executionResult = mapper.map(cobolResult);

    if (executionResult.status() == ACCEPTED) {
      portfolio.setCashBalance(BigDecimal.valueOf(executionResult.newCashBalance()));
      portfolioRepo.save(portfolio);

      Position position = optPos.orElseGet(Position::new);
      position.setPortfolio(portfolio);
      position.setSymbol(trade.symbol());
      position.setQuantity((long) executionResult.newQuantity());
      positionRepo.save(position);

      TradeHistory history = new TradeHistory();
      history.setPortfolio(portfolio);
      history.setAction(trade.action().getValue());
      history.setSymbol(trade.symbol());
      history.setQuantity((long) trade.quantity());
      history.setPrice(BigDecimal.valueOf(trade.price()));
      history.setTotalCost(BigDecimal.valueOf(executionResult.totalCost()));
      history.setCashBalanceAfter(BigDecimal.valueOf(executionResult.newCashBalance()));
      history.setExecutedAt(LocalDateTime.now());
      tradeHistoryRepo.save(history);
    }

    return executionResult;
  }
}
