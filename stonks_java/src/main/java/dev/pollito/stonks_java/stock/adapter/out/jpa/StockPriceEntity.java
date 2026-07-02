package dev.pollito.stonks_java.stock.adapter.out.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "stock_price")
public class StockPriceEntity {

  @Id
  @Column(name = "symbol", length = 4)
  private String symbol;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(
      name = "updated_at",
      nullable = false,
      columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime updatedAt;
}
