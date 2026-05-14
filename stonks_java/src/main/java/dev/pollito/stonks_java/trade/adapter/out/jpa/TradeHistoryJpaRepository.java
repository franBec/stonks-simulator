package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.TradeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeHistoryJpaRepository extends JpaRepository<TradeHistory, Long> {
  Page<TradeHistory> findByPortfolioIdOrderByExecutedAtDesc(Long portfolioId, Pageable pageable);
}
