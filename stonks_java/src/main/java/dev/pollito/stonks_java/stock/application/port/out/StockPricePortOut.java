package dev.pollito.stonks_java.stock.application.port.out;

import dev.pollito.stonks_java.stock.domain.StockPriceSnapshot;

public interface StockPricePortOut {
  StockPriceSnapshot loadCurrentPrices();

  void saveCurrentPrices(StockPriceSnapshot snapshot);

  void flushPriceSnapshot(StockPriceSnapshot snapshot);
}
