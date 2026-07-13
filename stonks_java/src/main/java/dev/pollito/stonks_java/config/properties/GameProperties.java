package dev.pollito.stonks_java.config.properties;

import java.math.BigDecimal;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.game")
public class GameProperties {
  private BigDecimal initialCash = new BigDecimal("10000.00");
  private double winThreshold = 100000.00;
  private double loseThreshold = 1000.00;
}
