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
                    new PositionSummary(
                        pos.getSymbol(),
                        pos.getQuantity().intValue(),
                        ZERO,
                        ZERO,
                        pos.getCostBasis(),
                        ZERO))
            .toList();
    return new PortfolioSummary(portfolio.getCashBalance(), positions, ZERO);
  }
}
