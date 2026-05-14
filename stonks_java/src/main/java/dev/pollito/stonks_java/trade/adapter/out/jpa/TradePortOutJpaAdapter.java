package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.trade.application.port.out.TradePortOutJpa;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradePortOutJpaAdapter implements TradePortOutJpa {

  private static final long PORTFOLIO_ID = 1L;

  private final TradeHistoryJpaRepository tradeHistoryJpaRepository;

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryJpaRepository
        .findByPortfolioIdOrderByExecutedAtDesc(PORTFOLIO_ID, pageable)
        .map(this::map);
  }

  private TradeHistoryItem map(dev.pollito.stonks_java.generated.entity.TradeHistory entity) {
    return new TradeHistoryItem(
        entity.getId(),
        entity.getAction(),
        entity.getSymbol(),
        entity.getQuantity().intValue(),
        entity.getPrice().doubleValue(),
        entity.getTotalCost().doubleValue(),
        entity.getCashBalanceAfter().doubleValue(),
        entity.getExecutedAt().atOffset(ZoneOffset.UTC));
  }
}
