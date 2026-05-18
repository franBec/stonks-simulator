package dev.pollito.stonks_java.news.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stonks.news")
public class NewsProperties {
  private Rss rss = new Rss();

  @Data
  public static class Rss {
    private List<String> feedUrls = new ArrayList<>();
  }
}
