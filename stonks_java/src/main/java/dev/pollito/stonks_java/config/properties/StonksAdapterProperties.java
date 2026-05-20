package dev.pollito.stonks_java.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.adapters")
public class StonksAdapterProperties {
  private DbMode db = DbMode.H2;
  private AdapterMode cobol = AdapterMode.STUB;
  private AdapterMode ai = AdapterMode.STUB;
  private AdapterMode news = AdapterMode.STUB;
  private boolean otel = false;

  public enum DbMode {
    H2,
    POSTGRESQL
  }

  public enum AdapterMode {
    STUB,
    REAL
  }
}
