// Profile-gated adapter — only active under stonks.adapters.ai=real
package dev.pollito.stonks_java.unit.real_adapter_out;

import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.pollito.stonks_java.chaosevent.adapter.out.ChaoticEventGeneratorFallbackAdapter;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChaoticEventGeneratorFallbackAdapterTest {

  @InjectMocks private ChaoticEventGeneratorFallbackAdapter adapter;

  private final List<NewsHeadline> headlines =
      List.of(
          new NewsHeadline(
              "Market News Today",
              "Test Source",
              "Tech",
              "http://example.com",
              OffsetDateTime.now()));

  private final List<StockPrice> stocks =
      List.of(
          new StockPrice(
              "GMEE",
              "GameStonk",
              valueOf(45),
              valueOf(44),
              valueOf(1),
              valueOf(2.27),
              Trend.MOON,
              valueOf(0.25),
              OffsetDateTime.now()));

  @Test
  void shouldGenerateEvent() {
    ChaoticEvent result = adapter.generate(headlines, stocks, null, null, null);

    assertNotNull(result);
    assertNotNull(result.headline());
    assertNotNull(result.explanation());
    assertNotNull(result.impactPercent());
    assertNotNull(result.type());
    assertNotNull(result.severity());
  }

  @Test
  void shouldUseTargetSymbolWhenProvided() {
    assertEquals("GMEE", adapter.generate(headlines, stocks, null, null, "GMEE").symbol());
  }

  @Test
  void shouldFilterByType() {
    assertEquals(
        ChaoticEventType.HYPE_WAVE,
        adapter.generate(headlines, stocks, ChaoticEventType.HYPE_WAVE, null, null).type());
  }

  @Test
  void shouldFilterBySeverity() {
    assertEquals(
        ChaoticEventSeverity.CRITICAL,
        adapter.generate(headlines, stocks, null, ChaoticEventSeverity.CRITICAL, null).severity());
  }

  @Test
  void shouldFilterByTypeAndSeverity() {
    ChaoticEvent result =
        adapter.generate(
            headlines, stocks, ChaoticEventType.RUG_PULL, ChaoticEventSeverity.CRITICAL, null);

    assertEquals(ChaoticEventType.RUG_PULL, result.type());
    assertEquals(ChaoticEventSeverity.CRITICAL, result.severity());
  }

  @Test
  void shouldDefaultSymbolToStockWhenEntryHasNoSymbol() {
    ChaoticEvent result =
        adapter.generate(headlines, stocks, ChaoticEventType.NEWS_FLASH, null, null);

    assertEquals(ChaoticEventType.NEWS_FLASH, result.type());
    assertNotNull(result.symbol());
  }

  @Test
  void shouldUseHeadlineAsSourceWhenAvailable() {
    assertEquals(
        "Market News Today",
        adapter.generate(headlines, stocks, null, null, null).sourceHeadline());
  }
}
