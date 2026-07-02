package dev.pollito.stonks_java.trade.adapter.out.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeHistoryJpaRepository extends JpaRepository<TradeHistoryEntity, Long> {
  Page<TradeHistoryEntity> findByPortfolioId(Long portfolioId, Pageable pageable);
}
