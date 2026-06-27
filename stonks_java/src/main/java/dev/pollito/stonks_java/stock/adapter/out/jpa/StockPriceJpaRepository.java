package dev.pollito.stonks_java.stock.adapter.out.jpa;

import dev.pollito.stonks_java.stock.adapter.out.jpa.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceJpaRepository extends JpaRepository<StockPriceEntity, String> {}
