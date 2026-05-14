package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.trade.adapter.out.jpa.mapper.TradeHistoryJpaMapper;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOutJpa;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeHistoryJpaAdapter implements TradeHistoryPortOutJpa {

  private static final long PORTFOLIO_ID = 1L;

  private final TradeHistoryJpaRepository tradeHistoryJpaRepository;
  private final TradeHistoryJpaMapper tradeHistoryJpaMapper;

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryJpaRepository
        .findByPortfolioIdOrderByExecutedAtDesc(PORTFOLIO_ID, pageable)
        .map(tradeHistoryJpaMapper::map);
  }
}
