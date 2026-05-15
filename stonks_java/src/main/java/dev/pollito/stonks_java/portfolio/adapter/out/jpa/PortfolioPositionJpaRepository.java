package dev.pollito.stonks_java.portfolio.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.Position;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioPositionJpaRepository extends JpaRepository<Position, Long> {
  List<Position> findByPortfolioId(Long portfolioId);
}
