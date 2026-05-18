package dev.pollito.stonks_java.chaos.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChaosEventGeneratorFallbackAdapterTest {

  private ChaosEventGeneratorFallbackAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ChaosEventGeneratorFallbackAdapter();
  }

  @Test
  void generatesEventWithEmptyInputs() {
    ChaosEvent event = adapter.generate(Collections.emptyList(), Collections.emptyList());
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
    ChaosEvent event =
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
    ChaosEvent first = adapter.generate(Collections.emptyList(), Collections.emptyList());
    ChaosEvent second = adapter.generate(Collections.emptyList(), Collections.emptyList());
    assertThat(first).isNotNull();
    assertThat(second).isNotNull();
  }

  @Test
  void impactPercentIsWithinReasonableRange() {
    ChaosEvent event = adapter.generate(Collections.emptyList(), Collections.emptyList());
    assertThat(event.impactPercent().doubleValue()).isBetween(-50.0, 51.0);
  }
}
