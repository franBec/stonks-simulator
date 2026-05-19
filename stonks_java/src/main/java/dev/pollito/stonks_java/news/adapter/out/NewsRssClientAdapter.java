package dev.pollito.stonks_java.news.adapter.out;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.empty;

import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import dev.pollito.stonks_java.news.adapter.out.mapper.NewsSyndMapper;
import dev.pollito.stonks_java.news.application.port.out.NewsClientPortOut;
import dev.pollito.stonks_java.news.config.NewsProperties;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.io.ByteArrayInputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Profile({"integrated", "production"})
@RequiredArgsConstructor
@Slf4j
public class NewsRssClientAdapter implements NewsClientPortOut {

  private final NewsProperties newsProperties;
  private final RestClient restClient;
  private final NewsSyndMapper mapper;

  @Override
  public List<NewsHeadline> fetchHeadlines() {
    return newsProperties.getRss().getFeedUrls().stream()
        .flatMap(
            feedUrl -> {
              try {
                return parseFeed(restClient.get().uri(feedUrl).retrieve().body(String.class))
                    .stream();
              } catch (Exception e) {
                log.error("Failed to fetch RSS feed: {}", feedUrl, e);
                return empty();
              }
            })
        .toList();
  }

  private List<NewsHeadline> parseFeed(String xml) {
    try {
      return new SyndFeedInput()
          .build(new XmlReader(new ByteArrayInputStream(xml.getBytes(UTF_8)))).getEntries().stream()
              .collect(
                  toMap(
                      e -> e.getTitle() != null ? e.getTitle().toLowerCase() : "",
                      mapper::toHeadline,
                      (a, b) -> b))
              .values()
              .stream()
              .toList();
    } catch (Exception e) {
      log.error("Failed to parse RSS XML", e);
      return emptyList();
    }
  }
}
