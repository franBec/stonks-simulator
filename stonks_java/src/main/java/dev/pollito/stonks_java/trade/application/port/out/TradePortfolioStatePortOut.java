package dev.pollito.stonks_java.trade.application.port.out;

import dev.pollito.stonks_java.trade.domain.TradePortfolioState;
import java.math.BigDecimal;

public interface TradePortfolioStatePortOut {
  TradePortfolioState getState(long portfolioId, String symbol);

  void applyExecution(
      long portfolioId,
      String symbol,
      BigDecimal newCashBalance,
      int newQuantity,
      BigDecimal costBasis);
}
