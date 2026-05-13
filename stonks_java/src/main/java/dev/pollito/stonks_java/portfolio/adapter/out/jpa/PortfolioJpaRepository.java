package dev.pollito.stonks_java.portfolio.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioJpaRepository extends JpaRepository<Portfolio, Long> {}
