package dev.pollito.stonks_java.portfolio.adapter.out.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioPositionJpaRepository extends JpaRepository<PositionEntity, Long> {
  List<PositionEntity> findByPortfolioId(Long portfolioId);
}
