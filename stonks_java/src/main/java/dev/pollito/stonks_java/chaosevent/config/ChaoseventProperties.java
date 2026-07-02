package dev.pollito.stonks_java.chaosevent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.chaos")
public class ChaoseventProperties {
  private boolean enabled = true;
  private int maxImpactPercent = 50;
}
