package dev.pollito.stonks_java.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.broadcast")
public class BroadcastProperties {
  private long sseTimeoutMs = 300_000L;
  private long heartbeatRateMs = 15_000L;
  private String tradePaperTapeFormat = "TRADE | %s %d %s @ $%.2f | TOTAL: $%.2f";
  private String paperTapeEntryFormat = "TRADE #%04d | %s %d %s @ $%.2f | TOTAL: $%.2f";
}
