package dev.pollito.stonks_java.trade.adapter.out.jpa;

import dev.pollito.stonks_java.portfolio.adapter.out.jpa.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradePortfolioJpaRepository extends JpaRepository<PortfolioEntity, Long> {}
