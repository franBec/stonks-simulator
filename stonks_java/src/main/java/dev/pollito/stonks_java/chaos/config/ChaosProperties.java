package dev.pollito.stonks_java.chaos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.chaos")
public class ChaosProperties {
  private boolean enabled = true;
  private int fallbackCatalogSize = 15;
  private long eventCheckIntervalMs = 10000;
}
