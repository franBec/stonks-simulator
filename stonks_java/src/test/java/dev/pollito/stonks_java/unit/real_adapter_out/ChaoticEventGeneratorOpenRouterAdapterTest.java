// Profile-gated adapter — only active under stonks.adapters.ai=real
package dev.pollito.stonks_java.unit.real_adapter_out;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaosevent.adapter.out.ChaoticEventGeneratorOpenRouterAdapter;
import dev.pollito.stonks_java.chaosevent.config.ChaoseventProperties;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventGenerationException;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class ChaoticEventGeneratorOpenRouterAdapterTest {

  private static final String VALID_JSON =
      """
      {
        "headline": "AI Generated Chaos",
        "symbol": "GMEE",
        "impactPercent": 15.0,
        "explanation": "Test explanation",
        "affectedSymbols": ["GMEE", "DOGE"],
        "sourceHeadline": "Original News",
        "occurredAt": "2024-06-15T12:00:00Z",
        "type": "HYPE_WAVE",
        "severity": "HIGH"
      }
      """;

  @Mock private ChatClient.Builder builder;
  @Mock private ChatClient chatClient;
  @Mock private ChatClient.ChatClientRequestSpec requestSpec;
  @Mock private ChatClient.CallResponseSpec callResponseSpec;
  @Mock private RateLimiterRegistry rateLimiterRegistry;
  @Mock private RateLimiter perMinuteRateLimiter;
  @Mock private RateLimiter perDayRateLimiter;
  @Mock private ChaoseventProperties chaoseventProperties;

  private ChaoticEventGeneratorOpenRouterAdapter adapter;
  private List<NewsHeadline> headlines;
  private List<StockPrice> stocks;

  @BeforeEach
  void setUp() {
    when(builder.build()).thenReturn(chatClient);
    when(rateLimiterRegistry.rateLimiter("ai-chaos-per-minute")).thenReturn(perMinuteRateLimiter);
    when(rateLimiterRegistry.rateLimiter("ai-chaos-per-day")).thenReturn(perDayRateLimiter);
    when(chaoseventProperties.getMaxImpactPercent()).thenReturn(50);

    adapter =
        new ChaoticEventGeneratorOpenRouterAdapter(
            builder, rateLimiterRegistry, chaoseventProperties);

    headlines =
        List.of(
            new NewsHeadline(
                "Original News",
                "Test Source",
                "Tech",
                "http://example.com",
                OffsetDateTime.now()));
    stocks =
        List.of(
            new StockPrice(
                "GMEE",
                "GameStonk",
                valueOf(45),
                valueOf(44),
                valueOf(1),
                valueOf(2.27),
                OffsetDateTime.now()),
            new StockPrice(
                "DOGE",
                "Dogecoin",
                valueOf(5),
                valueOf(4.9),
                valueOf(0.1),
                valueOf(2.04),
                OffsetDateTime.now()));
  }

  private void givenChatClientReturns(String response) {
    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.system(anyString())).thenReturn(requestSpec);
    when(requestSpec.user(anyString())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(callResponseSpec);
    when(callResponseSpec.content()).thenReturn(response);
  }

  private void givenRateLimitersAllow() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(true);
    when(perDayRateLimiter.acquirePermission()).thenReturn(true);
  }

  @Test
  void shouldGenerateEvent() {
    givenRateLimitersAllow();
    givenChatClientReturns(VALID_JSON);

    ChaoticEvent result =
        adapter.generate(
            headlines, stocks, ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.HIGH, "GMEE");

    assertEquals("AI Generated Chaos", result.headline());
    assertEquals("GMEE", result.symbol());
    assertEquals(valueOf(15.0), result.impactPercent());
    assertEquals("Test explanation", result.explanation());
    assertEquals(List.of("GMEE", "DOGE"), result.affectedSymbols());
    assertEquals("Original News", result.sourceHeadline());
    assertEquals(ChaoticEventType.HYPE_WAVE, result.type());
    assertEquals(ChaoticEventSeverity.HIGH, result.severity());
  }

  @Test
  void shouldThrowWhenPerMinuteRateLimited() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(false);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenPerDayRateLimited() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(true);
    when(perDayRateLimiter.acquirePermission()).thenReturn(false);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenAiReturnsNull() {
    givenRateLimitersAllow();
    givenChatClientReturns(null);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenAiReturnsBlank() {
    givenRateLimitersAllow();
    givenChatClientReturns("   ");

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenAiReturnsInvalidJson() {
    givenRateLimitersAllow();
    givenChatClientReturns("not valid json");

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenSymbolIsNull() {
    givenRateLimitersAllow();
    String json =
        """
        {
          "headline": "Test",
          "symbol": null,
          "impactPercent": 10.0,
          "explanation": "Test",
          "sourceHeadline": "Source",
          "occurredAt": "2024-01-01T00:00:00Z",
          "type": "HYPE_WAVE",
          "severity": "HIGH"
        }
        """;
    givenChatClientReturns(json);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenImpactPercentIsNull() {
    givenRateLimitersAllow();
    String json =
        """
        {
          "headline": "Test",
          "symbol": "GMEE",
          "impactPercent": null,
          "explanation": "Test",
          "sourceHeadline": "Source",
          "occurredAt": "2024-01-01T00:00:00Z",
          "type": "HYPE_WAVE",
          "severity": "HIGH"
        }
        """;
    givenChatClientReturns(json);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenSymbolNotInStockList() {
    givenRateLimitersAllow();
    String json =
        """
        {
          "headline": "Test",
          "symbol": "FAKE",
          "impactPercent": 10.0,
          "explanation": "Test",
          "sourceHeadline": "Source",
          "occurredAt": "2024-01-01T00:00:00Z",
          "type": "HYPE_WAVE",
          "severity": "HIGH"
        }
        """;
    givenChatClientReturns(json);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenImpactExceedsMax() {
    givenRateLimitersAllow();
    String json =
        """
        {
          "headline": "Test",
          "symbol": "GMEE",
          "impactPercent": 100.0,
          "explanation": "Test",
          "sourceHeadline": "Source",
          "occurredAt": "2024-01-01T00:00:00Z",
          "type": "HYPE_WAVE",
          "severity": "HIGH"
        }
        """;
    givenChatClientReturns(json);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldThrowWhenAffectedSymbolNotInStockList() {
    givenRateLimitersAllow();
    String json =
        """
        {
          "headline": "Test",
          "symbol": "GMEE",
          "impactPercent": 10.0,
          "explanation": "Test",
          "affectedSymbols": ["GMEE", "FAKE"],
          "sourceHeadline": "Source",
          "occurredAt": "2024-01-01T00:00:00Z",
          "type": "HYPE_WAVE",
          "severity": "HIGH"
        }
        """;
    givenChatClientReturns(json);

    assertThrows(
        ChaoticEventGenerationException.class,
        () -> adapter.generate(headlines, stocks, null, null, null));
  }

  @Test
  void shouldPassWhenAffectedSymbolsIsNull() {
    givenRateLimitersAllow();
    String json =
        """
        {
          "headline": "Test",
          "symbol": "GMEE",
          "impactPercent": 10.0,
          "explanation": "Test",
          "sourceHeadline": "Source",
          "occurredAt": "2024-01-01T00:00:00Z",
          "type": "HYPE_WAVE",
          "severity": "HIGH"
        }
        """;
    givenChatClientReturns(json);

    ChaoticEvent result = adapter.generate(headlines, stocks, null, null, null);

    assertEquals("GMEE", result.symbol());
    assertEquals(valueOf(10.0), result.impactPercent());
  }
}
