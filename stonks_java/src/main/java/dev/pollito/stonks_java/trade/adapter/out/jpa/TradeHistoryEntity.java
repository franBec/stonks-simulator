package dev.pollito.stonks_java.trade.adapter.out.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trade_history")
public class TradeHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "portfolio_id", nullable = false)
  private Long portfolioId;

  @Column(name = "action", nullable = false, length = 4)
  private String action;

  @Column(name = "symbol", nullable = false, length = 4)
  private String symbol;

  @Column(name = "quantity", nullable = false)
  private Long quantity;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalCost;

  @Column(name = "cash_balance_after", nullable = false, precision = 12, scale = 2)
  private BigDecimal cashBalanceAfter;

  @Column(name = "executed_at", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime executedAt;
}
