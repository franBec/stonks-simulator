package dev.pollito.stonks_java.stock.application.port.in;

import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;

public interface StockPortIn {
  void simulate();

  List<StockPrice> getStocks();
}
