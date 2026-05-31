package dev.pollito.stonks_java.unit.real_adapter_out;

import static java.time.OffsetDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rometools.rome.feed.synd.SyndEntry;
import dev.pollito.stonks_java.news.adapter.out.NewsRssClientAdapter;
import dev.pollito.stonks_java.news.adapter.out.mapper.NewsSyndMapper;
import dev.pollito.stonks_java.news.config.NewsProperties;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class NewsRssClientAdapterTest {

  private static final String FEED_URL = "https://example.com/feed.xml";
  private static final String RSS_XML =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Test Feed</title>
          <item>
            <title>Headline One</title>
            <link>https://example.com/1</link>
          </item>
          <item>
            <title>Headline Two</title>
            <link>https://example.com/2</link>
          </item>
        </channel>
      </rss>
      """;
  private static final String RSS_XML_WITH_NULL_TITLE =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Test Feed</title>
          <item>
            <title>Visible Headline</title>
            <link>https://example.com/1</link>
          </item>
          <item>
            <link>https://example.com/2</link>
          </item>
        </channel>
      </rss>
      """;
  private static final String DEDUP_RSS_XML =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>Test Feed</title>
          <item>
            <title>Same Headline</title>
            <link>https://example.com/1</link>
          </item>
          <item>
            <title>same headline</title>
            <link>https://example.com/2</link>
          </item>
        </channel>
      </rss>
      """;
  private static final OffsetDateTime NOW = now();
  private static final NewsHeadline HEADLINE_ONE =
      new NewsHeadline("Headline One", "Test Feed", null, "https://example.com/1", NOW);
  private static final NewsHeadline HEADLINE_TWO =
      new NewsHeadline("Headline Two", "Test Feed", null, "https://example.com/2", NOW);
  private static final NewsHeadline HEADLINE_NULL_TITLE =
      new NewsHeadline(null, "Test Feed", null, "https://example.com/2", NOW);
  private static final NewsHeadline HEADLINE_DUP_FIRST =
      new NewsHeadline("Same Headline", "Test Feed", null, "https://example.com/1", NOW);
  private static final NewsHeadline HEADLINE_DUP_LAST =
      new NewsHeadline("same headline", "Test Feed", null, "https://example.com/2", NOW);

  @Mock private NewsProperties newsProperties;
  @Mock private RestClient restClient;
  @Mock private NewsSyndMapper mapper;
  @InjectMocks private NewsRssClientAdapter adapter;

  private NewsProperties.Rss rss;

  @BeforeEach
  void setUp() {
    rss = new NewsProperties.Rss();
    when(newsProperties.getRss()).thenReturn(rss);
  }

  private void givenRestClientReturns(String body) {
    var requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
    var responseSpec = mock(RestClient.ResponseSpec.class);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(String.class)).thenReturn(body);
  }

  @Test
  void shouldFetchHeadlinesSuccessfully() {
    rss.setFeedUrls(List.of(FEED_URL));
    givenRestClientReturns(RSS_XML);
    when(mapper.toHeadline(any(SyndEntry.class))).thenReturn(HEADLINE_ONE, HEADLINE_TWO);

    List<NewsHeadline> result = adapter.fetchHeadlines();

    assertEquals(2, result.size());
    assertTrue(result.contains(HEADLINE_ONE));
    assertTrue(result.contains(HEADLINE_TWO));
  }

  @Test
  void shouldHandleHeadlinesWithoutTitle() {
    rss.setFeedUrls(List.of(FEED_URL));
    givenRestClientReturns(RSS_XML_WITH_NULL_TITLE);
    when(mapper.toHeadline(any(SyndEntry.class))).thenReturn(HEADLINE_ONE, HEADLINE_NULL_TITLE);

    List<NewsHeadline> result = adapter.fetchHeadlines();

    assertEquals(2, result.size());
    assertTrue(result.contains(HEADLINE_ONE));
    assertTrue(result.contains(HEADLINE_NULL_TITLE));
  }

  @Test
  void shouldReturnEmptyListWhenFeedReturnsNullBody() {
    rss.setFeedUrls(List.of(FEED_URL));
    givenRestClientReturns(null);

    assertTrue(adapter.fetchHeadlines().isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenFeedFetchThrowsException() {
    rss.setFeedUrls(List.of(FEED_URL));
    var requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(FEED_URL)).thenThrow(new RuntimeException("error"));

    assertTrue(adapter.fetchHeadlines().isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenFeedXmlIsMalformed() {
    rss.setFeedUrls(List.of(FEED_URL));
    givenRestClientReturns("not valid xml");

    assertTrue(adapter.fetchHeadlines().isEmpty());
  }

  @Test
  void shouldDeduplicateHeadlinesByLowercaseTitle() {
    rss.setFeedUrls(List.of(FEED_URL));
    givenRestClientReturns(DEDUP_RSS_XML);
    when(mapper.toHeadline(any(SyndEntry.class))).thenReturn(HEADLINE_DUP_FIRST, HEADLINE_DUP_LAST);

    List<NewsHeadline> result = adapter.fetchHeadlines();

    assertEquals(1, result.size());
    assertEquals("same headline", result.get(0).title());
  }
}
