package dev.pollito.stonks_java.stock.application.service;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.OffsetDateTime.now;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.application.port.out.PriceEnginePortOut;
import dev.pollito.stonks_java.stock.application.port.out.StockPortOut;
import dev.pollito.stonks_java.stock.domain.Stock;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService implements StockPortIn {

  private final PriceEnginePortOut priceEnginePortOut;
  private final StockPortOut stockPortOut;
  private final ApplicationEventPublisher events;

  private final ConcurrentHashMap<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, StockPrice> prices = new ConcurrentHashMap<>();

  @PostConstruct
  public void initialize() {
    OffsetDateTime now = now();
    for (Stock stock : stockPortOut.getStocks()) {
      currentPrices.put(stock.symbol(), stock.basePrice());
      prices.put(
          stock.symbol(),
          new StockPrice(
              stock.symbol(), stock.name(), stock.basePrice(), stock.basePrice(), ZERO, ZERO, now));
    }
  }

  @Override
  public void simulate() {
    List<Stock> stocks = stockPortOut.getStocks();
    OffsetDateTime now = now();
    for (Stock stock : stocks) {
      BigDecimal currentPrice = currentPrices.get(stock.symbol());
      if (currentPrice == null) continue;

      BigDecimal newPrice =
          priceEnginePortOut.calculate(currentPrice, stock.volatility(), stock.trend());
      currentPrices.put(stock.symbol(), newPrice);

      BigDecimal change = newPrice.subtract(currentPrice).setScale(2, HALF_UP);
      BigDecimal changePercent =
          currentPrice.compareTo(ZERO) > 0
              ? change.multiply(new BigDecimal("100")).divide(currentPrice, 2, HALF_UP)
              : ZERO;

      StockPrice stockPrice =
          new StockPrice(
              stock.symbol(), stock.name(), newPrice, currentPrice, change, changePercent, now);
      prices.put(stock.symbol(), stockPrice);

      events.publishEvent(new StockPriceUpdatedEvent(stockPrice));
    }
  }

  @Override
  public List<StockPrice> getStocks() {
    return new ArrayList<>(prices.values());
  }
}
