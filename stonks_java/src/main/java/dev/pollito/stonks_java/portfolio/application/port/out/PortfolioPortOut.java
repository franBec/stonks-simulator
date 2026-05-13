package dev.pollito.stonks_java.portfolio.application.port.out;

import dev.pollito.stonks_java.portfolio.domain.PortfolioSummary;

public interface PortfolioPortOut {
  PortfolioSummary getPortfolio();
}
