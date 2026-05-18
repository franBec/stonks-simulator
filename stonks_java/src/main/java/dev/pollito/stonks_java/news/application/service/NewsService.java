package dev.pollito.stonks_java.news.application.service;

import static java.util.stream.Collectors.toMap;

import dev.pollito.stonks_java.news.application.port.in.NewsPortIn;
import dev.pollito.stonks_java.news.application.port.out.NewsClientPortOut;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsService implements NewsPortIn {

  private final NewsClientPortOut newsClient;

  @Override
  @Cacheable(value = "headlines", unless = "#result.isEmpty()")
  // @Cacheable is in the service layer because stale headline data is a business decision:
  // serving headlines within the 60-second TTL is acceptable for generating chaos events.
  // This is performance optimization with a clear business constraint, not infrastructure.
  public List<NewsHeadline> getHeadlines() {
    return newsClient.fetchHeadlines().stream()
        .collect(
            toMap(
                h -> h.title().toLowerCase(), Function.identity(), (a, b) -> b, LinkedHashMap::new))
        .values()
        .stream()
        .toList();
  }
}
