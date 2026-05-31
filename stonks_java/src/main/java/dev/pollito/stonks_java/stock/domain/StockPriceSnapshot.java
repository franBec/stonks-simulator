package dev.pollito.stonks_java.stock.domain;

import java.math.BigDecimal;
import java.util.Map;

public record StockPriceSnapshot(Map<String, BigDecimal> prices) {
  public StockPriceSnapshot {
    prices = Map.copyOf(prices);
  }
}
