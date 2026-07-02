package dev.pollito.stonks_java.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.market.simulation")
public class MarketProperties {
  private long intervalMs = 5000;
}
