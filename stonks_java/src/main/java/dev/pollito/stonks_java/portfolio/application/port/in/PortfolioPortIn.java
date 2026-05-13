package dev.pollito.stonks_java.portfolio.application.port.in;

import dev.pollito.stonks_java.portfolio.domain.PortfolioSummary;

public interface PortfolioPortIn {
  PortfolioSummary getPortfolio();
}
