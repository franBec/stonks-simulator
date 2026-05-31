package dev.pollito.stonks_java.stock.adapter.out.jpa.mapper;

import static java.time.LocalDateTime.now;

import dev.pollito.stonks_java.generated.entity.StockPrice;
import dev.pollito.stonks_java.stock.domain.StockPriceSnapshot;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StockPriceJpaMapper {

  public List<StockPrice> toEntities(StockPriceSnapshot snapshot) {
    return snapshot.prices().entrySet().stream()
        .map(
            e ->
                StockPrice.builder()
                    .symbol(e.getKey())
                    .price(e.getValue())
                    .updatedAt(now())
                    .build())
        .toList();
  }

  public StockPriceSnapshot toSnapshot(List<StockPrice> entities) {
    Map<String, java.math.BigDecimal> prices =
        entities.stream().collect(Collectors.toMap(StockPrice::getSymbol, StockPrice::getPrice));
    return new StockPriceSnapshot(prices);
  }
}
