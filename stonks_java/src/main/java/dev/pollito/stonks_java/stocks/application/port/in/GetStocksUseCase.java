package dev.pollito.stonks_java.stocks.application.port.in;

import dev.pollito.stonks_java.stocks.domain.StockPrice;
import java.util.List;

public interface GetStocksUseCase {
  List<StockPrice> getStocks();
}
