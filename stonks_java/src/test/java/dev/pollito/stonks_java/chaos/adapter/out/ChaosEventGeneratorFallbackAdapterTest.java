package dev.pollito.stonks_java.chaos.adapter.out;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Unit test (not E2E) because the fallback adapter is pure algorithmic logic (random event
// generation) with no Spring or I/O dependencies. No HTTP endpoint exposes this functionality
// yet, making E2E impossible. Tests verify randomness range, empty/real inputs, and consistency.
class ChaosEventGeneratorFallbackAdapterTest {

  private ChaosEventGeneratorFallbackAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ChaosEventGeneratorFallbackAdapter();
  }

  @Test
  void generatesEventWithEmptyInputs() {
    var event = adapter.generate(emptyList(), emptyList(), null, null, null);
    assertThat(event).isNotNull();
    assertThat(event.headline()).isNotBlank();
    assertThat(event.symbol()).isNotBlank();
    assertThat(event.impactPercent()).isNotNull();
    assertThat(event.explanation()).isNotBlank();
    assertThat(event.affectedSymbols()).isNotEmpty();
    assertThat(event.sourceHeadline()).isEqualTo("Market Pulse");
    assertThat(event.occurredAt()).isNotNull();
    assertThat(event.type()).isNotNull();
    assertThat(event.severity()).isNotNull();
  }

  @Test
  void generatesEventWithRealInputs() {
    var event =
        adapter.generate(
            List.of(
                new dev.pollito.stonks_java.news.domain.NewsHeadline(
                    "Test Headline", "Test Source", "test", "https://example.com", now())),
            List.of(
                new dev.pollito.stonks_java.stock.domain.StockPrice(
                    "TEST", "Test Stock", valueOf(100), valueOf(99), ONE, valueOf(1.01), now())),
            null,
            null,
            null);
    assertThat(event).isNotNull();
    assertThat(event.headline()).isNotBlank();
    assertThat(event.sourceHeadline()).isEqualTo("Test Headline");
  }

  @Test
  void returnsRandomEventsOnMultipleCalls() {
    assertThat(adapter.generate(emptyList(), emptyList(), null, null, null)).isNotNull();
    assertThat(adapter.generate(emptyList(), emptyList(), null, null, null)).isNotNull();
  }

  @Test
  void impactPercentIsWithinReasonableRange() {
    assertThat(
            adapter
                .generate(emptyList(), emptyList(), null, null, null)
                .impactPercent()
                .doubleValue())
        .isBetween(-50.0, 51.0);
  }
}
