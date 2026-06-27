package dev.pollito.stonks_java.stock.adapter.out.jpa.mapper;

import static java.time.LocalDateTime.now;

import dev.pollito.stonks_java.stock.adapter.out.jpa.StockPriceEntity;
import dev.pollito.stonks_java.stock.domain.StockPriceSnapshot;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StockPriceJpaMapper {

  public List<StockPriceEntity> toEntities(StockPriceSnapshot snapshot) {
    return snapshot.prices().entrySet().stream()
        .map(
            e ->
                StockPriceEntity.builder()
                    .symbol(e.getKey())
                    .price(e.getValue())
                    .updatedAt(now())
                    .build())
        .toList();
  }

  public StockPriceSnapshot toSnapshot(List<StockPriceEntity> entities) {
    Map<String, java.math.BigDecimal> prices =
        entities.stream()
            .collect(Collectors.toMap(StockPriceEntity::getSymbol, StockPriceEntity::getPrice));
    return new StockPriceSnapshot(prices);
  }
}
