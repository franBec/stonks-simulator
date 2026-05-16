package dev.pollito.stonks_java.broadcast.domain;

import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.time.OffsetDateTime;

public record PriceTickBroadcastEvent(StockPrice stockPrice, OffsetDateTime occurredAt)
    implements BroadcastEvent {

  public PriceTickBroadcastEvent(StockPrice stockPrice) {
    this(stockPrice, OffsetDateTime.now());
  }

  @Override
  public BroadcastEventType type() {
    return BroadcastEventType.PRICE_TICK;
  }
}
