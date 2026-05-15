package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.Position;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradePositionJpaRepository extends JpaRepository<Position, Long> {
  Optional<Position> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);
}
