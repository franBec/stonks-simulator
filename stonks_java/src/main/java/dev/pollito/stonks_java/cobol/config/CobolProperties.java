package dev.pollito.stonks_java.cobol.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.cobol")
public class CobolProperties {
  private Map<String, ProgramConfig> programs = new HashMap<>();

  @Data
  public static class ProgramConfig {
    private String path;
    private int timeoutSeconds = 5;
  }
}
