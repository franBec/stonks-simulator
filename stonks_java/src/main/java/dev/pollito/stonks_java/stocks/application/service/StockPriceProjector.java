package dev.pollito.stonks_java.stocks.application.service;

import static java.math.BigDecimal.ZERO;
import static java.time.OffsetDateTime.now;

import dev.pollito.stonks_java.stocks.application.port.in.GetStocksUseCase;
import dev.pollito.stonks_java.stocks.application.port.out.CatalogPort;
import dev.pollito.stonks_java.stocks.domain.Stock;
import dev.pollito.stonks_java.stocks.domain.StockPrice;
import dev.pollito.stonks_java.stocks.domain.StockPriceUpdatedEvent;
import jakarta.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockPriceProjector implements GetStocksUseCase {

  private final CatalogPort catalogPort;
  private final ConcurrentHashMap<String, StockPrice> prices = new ConcurrentHashMap<>();

  @PostConstruct
  public void initialize() {
    OffsetDateTime now = now();
    for (Stock stock : catalogPort.getStocks()) {
      prices.put(
          stock.symbol(),
          new StockPrice(
              stock.symbol(), stock.name(), stock.basePrice(), stock.basePrice(), ZERO, ZERO, now));
    }
  }

  @ApplicationModuleListener
  public void onPriceUpdated(StockPriceUpdatedEvent event) {
    prices.put(event.stockPrice().symbol(), event.stockPrice());
  }

  @Override
  public List<StockPrice> getStocks() {
    return new ArrayList<>(prices.values());
  }
}
