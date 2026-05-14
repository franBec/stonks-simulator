package dev.pollito.stonks_java.trade.adapter.out.cobol;

import static dev.pollito.stonks_java.trade.domain.TradeAction.BUY;
import static dev.pollito.stonks_java.trade.domain.TradeAction.SELL;
import static dev.pollito.stonks_java.trade.domain.ValidationStatus.ACCEPTED;

import dev.pollito.stonks_java.generated.entity.Portfolio;
import dev.pollito.stonks_java.generated.entity.Position;
import dev.pollito.stonks_java.generated.entity.TradeHistory;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPortfolioJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeExecutionPositionJpaRepository;
import dev.pollito.stonks_java.trade.adapter.out.jpa.TradeHistoryJpaRepository;
import dev.pollito.stonks_java.trade.application.port.out.TradeExecutorPortOutCobol;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.ValidationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!cobol & !production")
@RequiredArgsConstructor
@Slf4j
public class PortfolioMgrCobolAdapterStub implements TradeExecutorPortOutCobol {

  private static final long PORTFOLIO_ID = 1L;

  private static final Set<String> VALID_SYMBOLS =
      Set.of("COBL", "GMEE", "DOGE", "TEND", "FOMO", "PAPR", "YOLO", "MEME", "BUGS", "JAVA");

  private final TradeExecutionPortfolioJpaRepository portfolioRepo;
  private final TradeExecutionPositionJpaRepository positionRepo;
  private final TradeHistoryJpaRepository tradeHistoryRepo;

  @Override
  @Transactional
  public TradeExecutionResult executeTrade(Trade trade) {
    log.warn("Using dev stub for TradeExecutorPortOutCobol — no real COBOL engine is running");

    Portfolio portfolio = portfolioRepo.findById(PORTFOLIO_ID).orElseThrow();
    double cashBalance = portfolio.getCashBalance().doubleValue();

    Optional<Position> optPos =
        positionRepo.findByPortfolioIdAndSymbol(PORTFOLIO_ID, trade.symbol());
    int holdingQty = optPos.map(p -> p.getQuantity().intValue()).orElse(0);

    if (trade.action() == null) {
      return rejected("S225", "JOB ABEND S225 - INVALID ACTION");
    }

    if (!VALID_SYMBOLS.contains(trade.symbol())) {
      return rejected("S001", "JOB ABEND S001 - UNKNOWN SYMBOL " + trade.symbol());
    }

    if (trade.quantity() <= 0) {
      return rejected("S224", "JOB ABEND S224 - INVALID QTY");
    }

    if (trade.price() <= 0.0) {
      return rejected("S226", "JOB ABEND S226 - INVALID PRICE");
    }

    double totalCost = trade.quantity() * trade.price();
    double newCashBalance;
    int newQuantity;

    if (trade.action() == BUY) {
      if (cashBalance < totalCost) {
        return rejected("S222", "JOB ABEND S222 - INSUFF FUNDS");
      }
      newCashBalance = cashBalance - totalCost;
      newQuantity = holdingQty + trade.quantity();
    } else if (trade.action() == SELL) {
      if (holdingQty < trade.quantity()) {
        return rejected("S223", "JOB ABEND S223 - INSUFF SHARES");
      }
      newCashBalance = cashBalance + totalCost;
      newQuantity = holdingQty - trade.quantity();
    } else {
      return rejected("S225", "JOB ABEND S225 - INVALID ACTION");
    }

    portfolio.setCashBalance(BigDecimal.valueOf(newCashBalance));
    portfolioRepo.save(portfolio);

    Position position = optPos.orElseGet(Position::new);
    position.setPortfolio(portfolio);
    position.setSymbol(trade.symbol());
    position.setQuantity((long) newQuantity);
    positionRepo.save(position);

    TradeHistory history = new TradeHistory();
    history.setPortfolio(portfolio);
    history.setAction(trade.action().getValue());
    history.setSymbol(trade.symbol());
    history.setQuantity((long) trade.quantity());
    history.setPrice(BigDecimal.valueOf(trade.price()));
    history.setTotalCost(BigDecimal.valueOf(totalCost));
    history.setCashBalanceAfter(BigDecimal.valueOf(newCashBalance));
    history.setExecutedAt(LocalDateTime.now());
    tradeHistoryRepo.save(history);

    return new TradeExecutionResult(
        ACCEPTED,
        null,
        "TRADE EXECUTED - " + trade.action().getValue() + " " + trade.symbol(),
        newCashBalance,
        newQuantity,
        totalCost);
  }

  private TradeExecutionResult rejected(String errorCode, String message) {
    return new TradeExecutionResult(ValidationStatus.REJECTED, errorCode, message, 0.0, 0, 0.0);
  }
}
