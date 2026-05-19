package dev.pollito.stonks_java.stock.application.service;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.OffsetDateTime.now;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.application.port.out.StockPortOut;
import dev.pollito.stonks_java.stock.application.port.out.StockPriceEnginePortOut;
import dev.pollito.stonks_java.stock.domain.Stock;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService implements StockPortIn {

  private final StockPriceEnginePortOut priceEnginePortOut;
  private final StockPortOut stockPortOut;
  private final ApplicationEventPublisher events;

  private final ConcurrentHashMap<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, StockPrice> prices = new ConcurrentHashMap<>();
  private final AtomicReference<BigDecimal> volatilityMultiplier =
      new AtomicReference<>(BigDecimal.ONE);
  private final ReentrantLock simulationLock = new ReentrantLock();
  private volatile List<Stock> catalog;

  @PostConstruct
  public void initialize() {
    catalog = stockPortOut.getStocks();
    OffsetDateTime now = now();
    for (Stock stock : catalog) {
      currentPrices.put(stock.symbol(), stock.basePrice());
      prices.put(
          stock.symbol(),
          new StockPrice(
              stock.symbol(), stock.name(), stock.basePrice(), stock.basePrice(), ZERO, ZERO, now));
    }
  }

  @Override
  public void simulate() {
    simulationLock.lock();
    try {
      List<Stock> stocks = catalog;
      OffsetDateTime now = now();
      BigDecimal multiplier = volatilityMultiplier.get();
      for (Stock stock : stocks) {
        BigDecimal currentPrice = currentPrices.get(stock.symbol());
        if (currentPrice == null) continue;

        BigDecimal effectiveVolatility = stock.volatility().multiply(multiplier);
        BigDecimal newPrice;
        try {
          newPrice = priceEnginePortOut.calculate(currentPrice, effectiveVolatility, stock.trend());
        } catch (Exception e) {
          log.warn("Price engine failed for {}, skipping tick: {}", stock.symbol(), e.getMessage());
          continue;
        }
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
    } finally {
      simulationLock.unlock();
    }
  }

  @Override
  public void applyImpact(String symbol, BigDecimal impactPercent) {
    simulationLock.lock();
    try {
      BigDecimal currentPrice = currentPrices.get(symbol);
      if (currentPrice == null) return;

      BigDecimal newPrice =
          currentPrice
              .multiply(ONE.add(impactPercent.divide(new BigDecimal("100"), 10, HALF_UP)))
              .setScale(2, HALF_UP);
      currentPrices.put(symbol, newPrice);

      StockPrice stockPrice = prices.get(symbol);
      if (stockPrice == null) return;

      BigDecimal change = newPrice.subtract(currentPrice).setScale(2, HALF_UP);
      BigDecimal changePercent =
          currentPrice.compareTo(ZERO) > 0
              ? change.multiply(new BigDecimal("100")).divide(currentPrice, 2, HALF_UP)
              : ZERO;

      StockPrice updated =
          new StockPrice(
              symbol, stockPrice.name(), newPrice, currentPrice, change, changePercent, now());
      prices.put(symbol, updated);

      events.publishEvent(new StockPriceUpdatedEvent(updated));
    } finally {
      simulationLock.unlock();
    }
  }

  @Override
  public List<StockPrice> getStocks() {
    return new ArrayList<>(prices.values());
  }

  @Override
  public void setVolatilityMultiplier(BigDecimal multiplier) {
    volatilityMultiplier.set(multiplier);
  }
}
