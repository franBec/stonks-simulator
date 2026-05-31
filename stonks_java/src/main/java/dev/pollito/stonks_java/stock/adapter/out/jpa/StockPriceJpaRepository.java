package dev.pollito.stonks_java.stock.adapter.out.jpa;

import dev.pollito.stonks_java.generated.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceJpaRepository extends JpaRepository<StockPrice, String> {}
