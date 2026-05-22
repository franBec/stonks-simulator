package dev.pollito.stonks_java.portfolio.application.service;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

import dev.pollito.stonks_java.portfolio.application.port.in.PortfolioPortIn;
import dev.pollito.stonks_java.portfolio.application.port.out.PortfolioPortOut;
import dev.pollito.stonks_java.portfolio.domain.PortfolioSummary;
import dev.pollito.stonks_java.portfolio.domain.PositionSummary;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService implements PortfolioPortIn {

  private final PortfolioPortOut portfolioPortOut;
  private final StockPortIn stockPortIn;

  @Override
  public PortfolioSummary getPortfolio() {
    var portfolio = portfolioPortOut.getPortfolio();

    List<PositionSummary> enrichedPositions =
        portfolio.positions().stream()
            .map(
                pos ->
                    enrichPosition(
                        pos,
                        stockPortIn.getStocks().stream()
                            .collect(toMap(StockPrice::symbol, StockPrice::price))))
            .toList();

    BigDecimal totalPnl =
        enrichedPositions.stream()
            .map(PositionSummary::unrealizedPnl)
            .reduce(ZERO, BigDecimal::add);

    return new PortfolioSummary(portfolio.cashBalance(), enrichedPositions, totalPnl);
  }

  private static PositionSummary enrichPosition(
      PositionSummary pos, Map<String, BigDecimal> priceMap) {
    BigDecimal currentPrice = priceMap.getOrDefault(pos.symbol(), ZERO);
    BigDecimal marketValue = currentPrice.multiply(BigDecimal.valueOf(pos.quantity()));
    BigDecimal unrealizedPnl = marketValue.subtract(pos.costBasis());
    return new PositionSummary(
        pos.symbol(), pos.quantity(), currentPrice, marketValue, pos.costBasis(), unrealizedPnl);
  }
}
