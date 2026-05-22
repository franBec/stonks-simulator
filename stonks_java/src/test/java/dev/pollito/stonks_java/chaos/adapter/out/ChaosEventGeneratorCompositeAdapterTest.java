package dev.pollito.stonks_java.chaos.adapter.out;

import static dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity.HIGH;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventType.HYPE_WAVE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaos.config.ChaosProperties;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class ChaosEventGeneratorCompositeAdapterTest {

  @Mock private ChatClient.Builder chatClientBuilder;
  @Mock private ChatClient chatClient;
  @Mock private RateLimiterRegistry rateLimiterRegistry;
  @Mock private RateLimiter perMinuteRateLimiter;
  @Mock private RateLimiter perDayRateLimiter;
  @Mock private ChaosProperties chaosProperties;

  private ChaosEventGeneratorOpenRouterAdapter openRouter;
  private ChaosEventGeneratorFallbackAdapter fallback;
  private ChaosEventGeneratorCompositeAdapter adapter;

  private static final String VALID_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":\"GMEE\",\"impactPercent\":20,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":[\"GMEE\"],\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  private static final String NULL_SYMBOL_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":null,\"impactPercent\":20,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":[\"GMEE\"],\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  private static final String NULL_IMPACT_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":\"GMEE\",\"impactPercent\":null,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":[\"GMEE\"],\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  private static final String FAKE_SYMBOL_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":\"FAKE\",\"impactPercent\":20,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":[\"GMEE\"],\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  private static final String ABSURD_IMPACT_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":\"GMEE\",\"impactPercent\":999,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":[\"GMEE\"],\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  private static final String FAKE_AFFECTED_SYMBOL_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":\"GMEE\",\"impactPercent\":20,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":[\"FAKE\"],\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  private static final String NULL_AFFECTED_SYMBOLS_JSON =
      "{\"headline\":\"AI Event\",\"symbol\":\"GMEE\",\"impactPercent\":20,\"explanation\":\"Generated"
          + " by"
          + " AI\",\"affectedSymbols\":null,\"sourceHeadline\":\"Source\",\"occurredAt\":\"2024-01-01T00:00:00Z\",\"type\":\"HYPE_WAVE\",\"severity\":\"HIGH\"}";

  @BeforeEach
  void setUp() {
    when(chaosProperties.getMaxImpactPercent()).thenReturn(50);
    when(rateLimiterRegistry.rateLimiter("ai-chaos-per-minute")).thenReturn(perMinuteRateLimiter);
    when(rateLimiterRegistry.rateLimiter("ai-chaos-per-day")).thenReturn(perDayRateLimiter);
    when(chatClientBuilder.build()).thenReturn(chatClient);

    openRouter =
        spy(
            new ChaosEventGeneratorOpenRouterAdapter(
                chatClientBuilder, rateLimiterRegistry, chaosProperties));
    fallback = spy(new ChaosEventGeneratorFallbackAdapter());
    adapter = new ChaosEventGeneratorCompositeAdapter(openRouter, fallback);
  }

  private static List<NewsHeadline> createHeadlines(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> new NewsHeadline("Title " + i, "Source " + i, null, null, null))
        .toList();
  }

  private static List<StockPrice> createStocks() {
    return of(
        new StockPrice("GMEE", "GMEE Inc", valueOf(10), valueOf(9), ONE, valueOf(0.1), now()));
  }

  private void mockChatClientContent(String content) {
    var promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
    var callResponseSpec = mock(ChatClient.CallResponseSpec.class);
    when(chatClient.prompt()).thenReturn(promptSpec);
    when(promptSpec.system(anyString())).thenReturn(promptSpec);
    when(promptSpec.user(anyString())).thenReturn(promptSpec);
    when(promptSpec.call()).thenReturn(callResponseSpec);
    when(callResponseSpec.content()).thenReturn(content);
  }

  @Test
  void returnsOpenRouterResultWhenItSucceeds() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(true);
    when(perDayRateLimiter.acquirePermission()).thenReturn(true);
    mockChatClientContent(VALID_JSON);

    var result = adapter.generate(createHeadlines(11), createStocks(), null, null, "GMEE");

    assertThat(result).isNotNull();
    assertThat(result.headline()).isEqualTo("AI Event");
    assertThat(result.symbol()).isEqualTo("GMEE");
    assertThat(result.impactPercent()).isEqualByComparingTo(valueOf(20));
    assertThat(result.explanation()).isEqualTo("Generated by AI");
    assertThat(result.affectedSymbols()).containsExactly("GMEE");
    assertThat(result.sourceHeadline()).isEqualTo("Source");
    assertThat(result.type()).isEqualTo(HYPE_WAVE);
    assertThat(result.severity()).isEqualTo(HIGH);
    assertThat(result.occurredAt()).isNotNull();
  }

  @Test
  void fallsBackOnOpenRouterFailure() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(false);

    var result = adapter.generate(emptyList(), emptyList(), null, null, null);

    assertThat(result).isNotNull();
    assertThat(result.headline()).isNotBlank();
    assertThat(result.symbol()).isNotBlank();
    assertThat(result.impactPercent()).isNotNull();
    assertThat(result.explanation()).isNotBlank();
    assertThat(result.affectedSymbols()).isNotEmpty();
    assertThat(result.sourceHeadline()).isNotBlank();
    assertThat(result.occurredAt()).isNotNull();
    assertThat(result.type()).isNotNull();
    assertThat(result.severity()).isNotNull();

    verify(fallback).generate(any(), any(), any(), any(), any());
  }

  @Test
  void perDayRateLimitExceeded_usesFallback() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(true);
    when(perDayRateLimiter.acquirePermission()).thenReturn(false);

    assertThat(adapter.generate(emptyList(), emptyList(), null, null, null)).isNotNull();
    verify(fallback).generate(any(), any(), any(), any(), any());
  }

  private static Stream<Arguments> fallbackCases() {
    return Stream.of(
        Arguments.of("null AI content", null, emptyList()),
        Arguments.of("blank AI content", "", emptyList()),
        Arguments.of("invalid JSON from AI", "{{{not json}}}", emptyList()),
        Arguments.of("null symbol from AI", NULL_SYMBOL_JSON, createStocks()),
        Arguments.of("null impactPercent from AI", NULL_IMPACT_JSON, createStocks()),
        Arguments.of("hallucinated symbol from AI", FAKE_SYMBOL_JSON, createStocks()),
        Arguments.of("absurd impact from AI", ABSURD_IMPACT_JSON, createStocks()),
        Arguments.of(
            "hallucinated affectedSymbol from AI", FAKE_AFFECTED_SYMBOL_JSON, createStocks()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fallbackCases")
  void usesFallbackWhenOpenRouterFails(String testName, String content, List<StockPrice> stocks) {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(true);
    when(perDayRateLimiter.acquirePermission()).thenReturn(true);
    mockChatClientContent(content);

    assertThat(adapter.generate(emptyList(), stocks, null, null, null)).isNotNull();
    verify(fallback).generate(any(), any(), any(), any(), any());
  }

  @Test
  void aiReturnsNullAffectedSymbols() {
    when(perMinuteRateLimiter.acquirePermission()).thenReturn(true);
    when(perDayRateLimiter.acquirePermission()).thenReturn(true);
    mockChatClientContent(NULL_AFFECTED_SYMBOLS_JSON);

    var result = adapter.generate(emptyList(), createStocks(), null, null, null);

    assertThat(result).isNotNull();
    assertThat(result.affectedSymbols()).isNull();
    assertThat(result.symbol()).isEqualTo("GMEE");
  }
}
