package dev.pollito.stonks_java.portfolio.adapter.out.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioJpaRepository extends JpaRepository<PortfolioEntity, Long> {}
