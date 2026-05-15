package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradePortfolioJpaRepository extends JpaRepository<Portfolio, Long> {}
