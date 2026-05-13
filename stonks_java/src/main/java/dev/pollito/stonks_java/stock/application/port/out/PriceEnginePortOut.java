package dev.pollito.stonks_java.stock.application.port.out;

import java.math.BigDecimal;

public interface PriceEnginePortOut {
  BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, String trend);
}
