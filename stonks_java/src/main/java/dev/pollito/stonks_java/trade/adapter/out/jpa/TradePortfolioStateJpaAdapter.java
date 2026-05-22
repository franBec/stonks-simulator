package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.trade.application.port.out.TradePortfolioStatePortOut;
import dev.pollito.stonks_java.trade.domain.TradePortfolioState;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradePortfolioStateJpaAdapter implements TradePortfolioStatePortOut {

  private final TradePortfolioJpaRepository portfolioRepo;
  private final TradePositionJpaRepository positionRepo;

  @Override
  public TradePortfolioState getState(long portfolioId, String symbol) {
    var portfolio = portfolioRepo.findById(portfolioId).orElseThrow();
    var optPos = positionRepo.findByPortfolioIdAndSymbol(portfolioId, symbol);
    return new TradePortfolioState(
        portfolio.getCashBalance().doubleValue(),
        optPos.map(p -> p.getQuantity().intValue()).orElse(0),
        optPos.map(p -> p.getCostBasis().doubleValue()).orElse(0.0));
  }

  @Override
  public void applyExecution(
      long portfolioId,
      String symbol,
      BigDecimal newCashBalance,
      int newQuantity,
      BigDecimal costBasis) {
    var portfolio = portfolioRepo.findById(portfolioId).orElseThrow();
    portfolio.setCashBalance(newCashBalance);
    portfolioRepo.save(portfolio);

    var position =
        positionRepo
            .findByPortfolioIdAndSymbol(portfolioId, symbol)
            .orElseGet(dev.pollito.stonks_java.generated.entity.Position::new);
    position.setPortfolio(portfolio);
    position.setSymbol(symbol);
    position.setQuantity((long) newQuantity);
    position.setCostBasis(costBasis);
    positionRepo.save(position);
  }
}
