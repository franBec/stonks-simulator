package dev.pollito.stonks_java.stock.application.port.in;

import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.math.BigDecimal;
import java.util.List;

public interface StockPortIn {
  void simulate();

  void applyImpact(String symbol, BigDecimal impactPercent);

  List<StockPrice> getStocks();
}
