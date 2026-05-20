package dev.pollito.stonks_java.news.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.news.adapter.out.mapper.NewsSyndMapper;
import dev.pollito.stonks_java.news.adapter.out.mapper.NewsSyndMapperImpl;
import dev.pollito.stonks_java.news.config.NewsProperties;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

// Unit test (not E2E) because NewsRssClientAdapter is @Profile({"integrated", "production"})
// — never loaded under the default (H2 + stubs) profile used by E2E tests. Verifies feed
// fetching, XML parsing, and per-feed error isolation with a mock RestClient and real
// NewsSyndMapperImpl spy so the mapping logic is exercised.
@ExtendWith(MockitoExtension.class)
class NewsRssClientAdapterTest {

  private static final String RSS_XML =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Test Feed</title>
          <item>
            <title>Test Headline</title>
            <link>https://example.com/1</link>
            <source>Source1</source>
            <category>tech</category>
            <pubDate>Mon, 01 Jan 2024 00:00:00 GMT</pubDate>
          </item>
          <item>
            <title>Second Story</title>
            <link>https://example.com/2</link>
            <source>Source2</source>
            <category>markets</category>
            <pubDate>Tue, 02 Jan 2024 00:00:00 GMT</pubDate>
          </item>
        </channel>
      </rss>
      """;

  @Mock private RestClient restClient;
  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private RestClient.ResponseSpec responseSpec;
  @Mock private NewsProperties newsProperties;

  @Spy private NewsSyndMapper mapper = new NewsSyndMapperImpl();

  @InjectMocks private NewsRssClientAdapter adapter;

  private void configureFeeds(List<String> feedUrls) {
    var rss = new NewsProperties.Rss();
    rss.setFeedUrls(feedUrls);
    when(newsProperties.getRss()).thenReturn(rss);
  }

  @Test
  void fetchHeadlinesReturnsParsedHeadlines() {
    configureFeeds(List.of("https://example.com/rss"));
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class)).thenReturn(RSS_XML);

    List<NewsHeadline> headlines = adapter.fetchHeadlines();

    assertThat(headlines).hasSize(2);
    assertThat(headlines.get(0).title()).isEqualTo("Test Headline");
    assertThat(headlines.get(0).url()).isEqualTo("https://example.com/1");
    assertThat(headlines.get(1).title()).isEqualTo("Second Story");
  }

  @Test
  void fetchHeadlinesIsolatesPerFeedErrors() {
    configureFeeds(List.of("https://example.com/fail", "https://example.com/rss"));
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class))
        .thenThrow(new RuntimeException("Connection refused"))
        .thenReturn(RSS_XML);

    assertThat(adapter.fetchHeadlines()).hasSize(2);
  }

  @Test
  void fetchHeadlinesReturnsEmptyForAllFailedFeeds() {
    configureFeeds(List.of("https://example.com/fail1", "https://example.com/fail2"));
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class))
        .thenThrow(new RuntimeException("Timeout"))
        .thenThrow(new RuntimeException("DNS failure"));

    assertThat(adapter.fetchHeadlines()).isEmpty();
  }
}
