package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.trade.adapter.out.jpa.mapper.TradeExecutionEntityMapper;
import dev.pollito.stonks_java.trade.adapter.out.jpa.mapper.TradeHistoryJpaMapper;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOut;
import dev.pollito.stonks_java.trade.domain.Trade;
import dev.pollito.stonks_java.trade.domain.TradeExecutionResult;
import dev.pollito.stonks_java.trade.domain.TradeHistoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TradeHistoryJpaAdapter implements TradeHistoryPortOut {

  private static final long PORTFOLIO_ID = 1L;

  private final TradeHistoryJpaRepository tradeHistoryJpaRepository;
  private final TradeHistoryJpaMapper tradeHistoryJpaMapper;
  private final TradeExecutionEntityMapper historyEntityMapper;

  @Override
  public Page<TradeHistoryItem> getTradeHistory(Pageable pageable) {
    return tradeHistoryJpaRepository
        .findByPortfolioId(PORTFOLIO_ID, pageable)
        .map(tradeHistoryJpaMapper::map);
  }

  @Override
  public void recordExecution(Trade trade, TradeExecutionResult result, long portfolioId) {
    tradeHistoryJpaRepository.save(historyEntityMapper.map(trade, result, portfolioId));
  }

  @Override
  @Transactional
  public void clearHistory() {
    tradeHistoryJpaRepository.deleteAll();
  }
}
