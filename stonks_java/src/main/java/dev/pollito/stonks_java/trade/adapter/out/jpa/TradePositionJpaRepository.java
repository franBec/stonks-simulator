package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.portfolio.adapter.out.jpa.PositionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradePositionJpaRepository extends JpaRepository<PositionEntity, Long> {
  Optional<PositionEntity> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}
