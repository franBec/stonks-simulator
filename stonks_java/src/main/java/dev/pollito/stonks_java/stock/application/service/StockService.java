package dev.pollito.stonks_java.stock.application.service;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toMap;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.application.port.out.StockCatalogPortOut;
import dev.pollito.stonks_java.stock.application.port.out.StockPriceEnginePortOut;
import dev.pollito.stonks_java.stock.application.port.out.StockPricePortOut;
import dev.pollito.stonks_java.stock.domain.ApplyStockImpact;
import dev.pollito.stonks_java.stock.domain.Stock;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceSnapshot;
import dev.pollito.stonks_java.stock.domain.StockPriceUpdatedEvent;
import dev.pollito.stonks_java.stock.domain.Trend;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService implements StockPortIn {

  private final StockPriceEnginePortOut priceEnginePortOut;
  private final StockCatalogPortOut stockCatalogPortOut;
  private final StockPricePortOut stockPricePortOut;
  private final ApplicationEventPublisher events;

  @Value("${stonks.market.price.persist-interval-ms:60000}")
  private long persistIntervalMs;

  private final ConcurrentHashMap<String, StockPrice> prices = new ConcurrentHashMap<>();
  private final AtomicReference<BigDecimal> volatilityMultiplier =
      new AtomicReference<>(BigDecimal.ONE);
  private final ReentrantLock simulationLock = new ReentrantLock();
  private final AtomicReference<Instant> lastPersistAt = new AtomicReference<>(Instant.EPOCH);
  private volatile List<Stock> catalog;

  @PostConstruct
  public void initialize() {
    catalog = stockCatalogPortOut.getStocks();
    StockPriceSnapshot persisted = stockPricePortOut.loadCurrentPrices();
    OffsetDateTime now = OffsetDateTime.now();
    for (Stock stock : catalog) {
      BigDecimal price = persisted.prices().getOrDefault(stock.symbol(), stock.basePrice());
      prices.put(
          stock.symbol(),
          new StockPrice(
              stock.symbol(),
              stock.name(),
              stock.description(),
              price,
              price,
              ZERO,
              ZERO,
              stock.trend(),
              stock.volatility(),
              now));
    }
  }

  @Override
  public void simulate() {
    simulationLock.lock();
    try {
      for (Stock stock : catalog) {
        StockPrice previous = prices.get(stock.symbol());
        if (previous == null) continue;
        BigDecimal currentPrice = previous.price();

        BigDecimal newPrice =
            priceEnginePortOut.calculate(
                currentPrice,
                stock.volatility().multiply(volatilityMultiplier.get()),
                stock.trend());

        StockPrice stockPrice =
            buildStockPrice(
                stock.symbol(),
                stock.name(),
                stock.description(),
                newPrice,
                currentPrice,
                stock.trend(),
                stock.volatility(),
                OffsetDateTime.now());
        prices.put(stock.symbol(), stockPrice);

        events.publishEvent(new StockPriceUpdatedEvent(stockPrice));
      }
      tryPersistPrices();
    } finally {
      simulationLock.unlock();
    }
  }

  @EventListener
  void onApplyStockImpact(ApplyStockImpact event) {
    applyImpact(event.symbol(), event.impactPercent());
  }

  private void applyImpact(String symbol, BigDecimal impactPercent) {
    simulationLock.lock();
    try {
      StockPrice existing = prices.get(symbol);
      if (existing == null) return;
      BigDecimal currentPrice = existing.price();

      BigDecimal newPrice =
          currentPrice
              .multiply(ONE.add(impactPercent.divide(new BigDecimal("100"), 10, HALF_UP)))
              .setScale(2, HALF_UP);

      StockPrice updated =
          buildStockPrice(
              symbol,
              existing.name(),
              existing.description(),
              newPrice,
              currentPrice,
              existing.trend(),
              existing.volatility(),
              OffsetDateTime.now());
      prices.put(symbol, updated);

      events.publishEvent(new StockPriceUpdatedEvent(updated));

      tryPersistPrices();
    } finally {
      simulationLock.unlock();
    }
  }

  private void tryPersistPrices() {
    Instant now = now();
    if (between(lastPersistAt.get(), now).toMillis() >= persistIntervalMs) {
      stockPricePortOut.saveCurrentPrices(currentPricesSnapshot());
      lastPersistAt.set(now);
    }
  }

  @PreDestroy
  public void persistOnShutdown() {
    log.info("Persisting current prices before shutdown");
    stockPricePortOut.flushPriceSnapshot(currentPricesSnapshot());
  }

  @Override
  public List<StockPrice> getStocks() {
    return new ArrayList<>(prices.values());
  }

  @Override
  public void setVolatilityMultiplier(BigDecimal multiplier) {
    volatilityMultiplier.set(multiplier);
  }

  private StockPrice buildStockPrice(
      String symbol,
      String name,
      String description,
      BigDecimal newPrice,
      BigDecimal currentPrice,
      Trend trend,
      BigDecimal volatility,
      OffsetDateTime now) {
    BigDecimal change = newPrice.subtract(currentPrice).setScale(2, HALF_UP);
    BigDecimal changePercent =
        currentPrice.compareTo(ZERO) > 0
            ? change.multiply(new BigDecimal("100")).divide(currentPrice, 2, HALF_UP)
            : ZERO;
    return new StockPrice(
        symbol, name, description, newPrice, currentPrice, change, changePercent, trend, volatility, now);
  }

  private StockPriceSnapshot currentPricesSnapshot() {
    return new StockPriceSnapshot(
        prices.entrySet().stream().collect(toMap(Entry::getKey, e -> e.getValue().price())));
  }
}
