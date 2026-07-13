// Profile-gated adapter — only active under stonks.adapters.ai=real
package dev.pollito.stonks_java.unit.real_adapter_out;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.chaosevent.adapter.out.ChaoticEventGeneratorCompositeAdapter;
import dev.pollito.stonks_java.chaosevent.adapter.out.ChaoticEventGeneratorFallbackAdapter;
import dev.pollito.stonks_java.chaosevent.adapter.out.ChaoticEventGeneratorOpenRouterAdapter;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import dev.pollito.stonks_java.stock.domain.Trend;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChaoticEventGeneratorCompositeAdapterTest {

  @Mock private ChaoticEventGeneratorOpenRouterAdapter openRouter;
  @Mock private ChaoticEventGeneratorFallbackAdapter fallback;
  @InjectMocks private ChaoticEventGeneratorCompositeAdapter adapter;

  private final List<NewsHeadline> headlines =
      List.of(
          new NewsHeadline(
              "Test News", "Test Source", "Tech", "http://example.com", OffsetDateTime.now()));

  private final List<StockPrice> stocks =
      List.of(
          new StockPrice(
              "GMEE",
              "GameStonk",
              "To the moon!",
              valueOf(45),
              valueOf(44),
              valueOf(1),
              valueOf(2.27),
              Trend.MOON,
              valueOf(0.25),
              OffsetDateTime.now()));

  private final ChaoticEvent expectedEvent =
      new ChaoticEvent(
          "Test Event",
          "GMEE",
          valueOf(10),
          "Explanation",
          List.of("GMEE"),
          "News",
          OffsetDateTime.now(),
          ChaoticEventType.HYPE_WAVE,
          ChaoticEventSeverity.HIGH,
          null);

  @Test
  void shouldReturnOpenRouterResultWhenItSucceeds() {
    when(openRouter.generate(anyList(), anyList(), any(), any(), anyString()))
        .thenReturn(expectedEvent);

    assertEquals(
        expectedEvent,
        adapter.generate(
            headlines, stocks, ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.HIGH, "GMEE"));
    verify(openRouter)
        .generate(headlines, stocks, ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.HIGH, "GMEE");
  }

  @Test
  void shouldFallbackWhenOpenRouterThrows() {
    when(openRouter.generate(anyList(), anyList(), any(), any(), anyString()))
        .thenThrow(new RuntimeException("AI service unavailable"));
    when(fallback.generate(anyList(), anyList(), any(), any(), anyString()))
        .thenReturn(expectedEvent);

    assertEquals(
        expectedEvent,
        adapter.generate(
            headlines, stocks, ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.HIGH, "GMEE"));
    verify(fallback)
        .generate(headlines, stocks, ChaoticEventType.HYPE_WAVE, ChaoticEventSeverity.HIGH, "GMEE");
  }
}
