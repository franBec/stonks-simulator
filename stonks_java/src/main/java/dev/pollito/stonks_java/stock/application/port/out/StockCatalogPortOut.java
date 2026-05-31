package dev.pollito.stonks_java.stock.application.port.out;

import dev.pollito.stonks_java.stock.domain.Stock;
import java.util.List;

public interface StockCatalogPortOut {
  List<Stock> getStocks();
}
