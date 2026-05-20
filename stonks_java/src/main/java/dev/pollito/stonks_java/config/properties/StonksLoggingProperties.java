package dev.pollito.stonks_java.config.properties;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.logging")
public class StonksLoggingProperties {
  private AdapterOut adapterOut = new AdapterOut();

  @Data
  public static class AdapterOut {
    private List<String> excludePatterns = List.of();
  }
}
