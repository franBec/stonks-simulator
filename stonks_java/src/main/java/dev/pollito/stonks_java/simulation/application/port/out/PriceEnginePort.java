package dev.pollito.stonks_java.simulation.application.port.out;

import java.math.BigDecimal;

public interface PriceEnginePort {
  BigDecimal calculate(BigDecimal currentPrice, BigDecimal volatility, String trend);
}
