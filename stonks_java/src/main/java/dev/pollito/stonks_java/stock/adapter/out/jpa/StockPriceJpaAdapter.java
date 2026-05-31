package dev.pollito.stonks_java.stock.adapter.out.jpa;

import dev.pollito.stonks_java.stock.adapter.out.jpa.mapper.StockPriceJpaMapper;
import dev.pollito.stonks_java.stock.application.port.out.StockPricePortOut;
import dev.pollito.stonks_java.stock.domain.StockPriceSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockPriceJpaAdapter implements StockPricePortOut {

  private final StockPriceJpaRepository repo;
  private final StockPriceJpaMapper mapper;

  @Override
  public StockPriceSnapshot loadCurrentPrices() {
    return mapper.toSnapshot(repo.findAll());
  }

  @Override
  @Async("pricePersistExecutor")
  public void saveCurrentPrices(StockPriceSnapshot snapshot) {
    repo.saveAll(mapper.toEntities(snapshot));
  }

  @Override
  public void flushPriceSnapshot(StockPriceSnapshot snapshot) {
    repo.saveAll(mapper.toEntities(snapshot));
  }
}
