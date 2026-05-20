package dev.pollito.stonks_java.news;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.news.application.port.out.NewsClientPortOut;
import dev.pollito.stonks_java.news.application.service.NewsService;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Unit test (not E2E) because the NewsClientStub active under the default profile never returns
// duplicate-titled headlines, so the dedup merge function in NewsService.getHeadlines() is
// unreachable from E2E tests. This test mocks the client to produce duplicates and verifies
// the merge function (a, b) -> b correctly keeps one.
@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  @Mock private NewsClientPortOut newsClient;
  @InjectMocks private NewsService newsService;

  @Test
  void deduplicatesHeadlinesByLowercaseTitle() {
    var now = now();
    when(newsClient.fetchHeadlines())
        .thenReturn(
            List.of(
                new NewsHeadline("Breaking News", "Source1", "tech", "https://example.com/1", now),
                new NewsHeadline(
                    "breaking news", "Source2", "markets", "https://example.com/2", now),
                new NewsHeadline(
                    "Unique Story", "Source3", "economy", "https://example.com/3", now)));

    var headlines = newsService.getHeadlines();

    assertThat(headlines).hasSize(2);
    assertThat(headlines.stream().map(NewsHeadline::title))
        .containsExactlyInAnyOrder("breaking news", "Unique Story");
  }
}
