package dev.pollito.stonks_java.stock.application.port.out;

import dev.pollito.stonks_java.stock.domain.Trend;
import java.math.BigDecimal;

public interface StockPriceEnginePortOut {
  BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, Trend trend);
}
