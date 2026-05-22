package dev.pollito.stonks_java.portfolio.adapter.out.jpa;

import static java.math.BigDecimal.ZERO;

import dev.pollito.stonks_java.portfolio.application.port.out.PortfolioPortOut;
import dev.pollito.stonks_java.portfolio.domain.PortfolioSummary;
import dev.pollito.stonks_java.portfolio.domain.PositionSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PortfolioJpaAdapter implements PortfolioPortOut {

  private static final long PORTFOLIO_ID = 1L;

  private final PortfolioJpaRepository portfolioRepo;
  private final PortfolioPositionJpaRepository positionRepo;

  @Override
  public PortfolioSummary getPortfolio() {
    var portfolio = portfolioRepo.findById(PORTFOLIO_ID).orElseThrow();
    List<PositionSummary> positions =
        positionRepo.findByPortfolioId(PORTFOLIO_ID).stream()
            .map(
                pos ->
                    // FIXME: costBasis is hard-coded to ZERO because the `position` table schema
                    // does not have a cost_basis column. This makes unrealizedPnl always equal to
                    // marketValue, which is incorrect. To fix:
                    //  1. Add `cost_basis DECIMAL(12,2) NOT NULL DEFAULT 0` to the position table
                    //  2. Update TradePortfolioStateJpaAdapter.applyExecution to pass price/totalCost
                    //  3. Update generated Position entity to include costBasis field
                    //  4. Read costBasis from the entity here instead of ZERO
                    new PositionSummary(
                        pos.getSymbol(), pos.getQuantity().intValue(), ZERO, ZERO, ZERO, ZERO))
            .toList();
    return new PortfolioSummary(portfolio.getCashBalance(), positions, ZERO);
  }
}
