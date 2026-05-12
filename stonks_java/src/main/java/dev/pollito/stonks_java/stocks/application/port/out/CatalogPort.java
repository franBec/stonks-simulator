package dev.pollito.stonks_java.stocks.application.port.out;

import dev.pollito.stonks_java.stocks.domain.Stock;
import java.util.List;

public interface CatalogPort {
  List<Stock> getStocks();
}
