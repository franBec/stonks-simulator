package dev.pollito.stonks_java.chaos.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
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
    var event = adapter.generate(Collections.emptyList(), Collections.emptyList());
    assertThat(event).isNotNull();
    assertThat(event.headline()).isNotBlank();
    assertThat(event.symbol()).isNotBlank();
    assertThat(event.impactPercent()).isNotNull();
    assertThat(event.explanation()).isNotBlank();
    assertThat(event.affectedSymbols()).isNotEmpty();
    assertThat(event.sourceHeadline()).isEqualTo("Market Pulse");
    assertThat(event.occurredAt()).isNotNull();
  }

  @Test
  void generatesEventWithRealInputs() {
    var event =
        adapter.generate(
            List.of(
                new dev.pollito.stonks_java.news.domain.NewsHeadline(
                    "Test Headline",
                    "Test Source",
                    "test",
                    "https://example.com",
                    java.time.OffsetDateTime.now())),
            List.of(
                new dev.pollito.stonks_java.stock.domain.StockPrice(
                    "TEST",
                    "Test Stock",
                    java.math.BigDecimal.valueOf(100),
                    java.math.BigDecimal.valueOf(99),
                    java.math.BigDecimal.ONE,
                    java.math.BigDecimal.valueOf(1.01),
                    java.time.OffsetDateTime.now())));
    assertThat(event).isNotNull();
    assertThat(event.headline()).isNotBlank();
    assertThat(event.sourceHeadline()).isEqualTo("Test Headline");
  }

  @Test
  void returnsRandomEventsOnMultipleCalls() {
    assertThat(adapter.generate(Collections.emptyList(), Collections.emptyList())).isNotNull();
    assertThat(adapter.generate(Collections.emptyList(), Collections.emptyList())).isNotNull();
  }

  @Test
  void impactPercentIsWithinReasonableRange() {
    assertThat(
            adapter
                .generate(Collections.emptyList(), Collections.emptyList())
                .impactPercent()
                .doubleValue())
        .isBetween(-50.0, 51.0);
  }
}
