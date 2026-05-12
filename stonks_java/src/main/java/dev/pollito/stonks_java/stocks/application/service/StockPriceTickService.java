package dev.pollito.stonks_java.stocks.application.service;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import dev.pollito.stonks_java.simulation.application.port.out.PriceEnginePort;
import dev.pollito.stonks_java.stocks.application.port.in.GetStocksUseCase;
import dev.pollito.stonks_java.stocks.application.port.out.CatalogPort;
import dev.pollito.stonks_java.stocks.domain.Stock;
import dev.pollito.stonks_java.stocks.domain.StockPrice;
import dev.pollito.stonks_java.stocks.domain.StockPriceUpdatedEvent;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockPriceTickService {

  private final PriceEnginePort priceEnginePort;
  private final CatalogPort catalogPort;
  private final GetStocksUseCase getStocksUseCase;
  private final ApplicationEventPublisher events;

  public void simulate() {
    Map<String, StockPrice> currentPrices =
        getStocksUseCase.getStocks().stream().collect(toMap(StockPrice::symbol, identity()));
    List<Stock> stocks = catalogPort.getStocks();

    OffsetDateTime now = OffsetDateTime.now();
    for (Stock stock : stocks) {
      StockPrice current = currentPrices.get(stock.symbol());
      if (current == null) continue;

      BigDecimal newPrice =
          priceEnginePort.calculate(current.price(), stock.volatility(), stock.trend());

      BigDecimal change = newPrice.subtract(current.price()).setScale(2, HALF_UP);
      BigDecimal changePercent =
          current.price().compareTo(ZERO) > 0
              ? change.multiply(new BigDecimal("100")).divide(current.price(), 2, HALF_UP)
              : ZERO;

      events.publishEvent(
          new StockPriceUpdatedEvent(
              new StockPrice(
                  stock.symbol(),
                  stock.name(),
                  newPrice,
                  current.price(),
                  change,
                  changePercent,
                  now)));
    }
  }
}
