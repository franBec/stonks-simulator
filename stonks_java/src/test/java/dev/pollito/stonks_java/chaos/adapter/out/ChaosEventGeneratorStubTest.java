package dev.pollito.stonks_java.chaos.adapter.out;

import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Unit test (not E2E) because the stub is a pure in-memory algorithm with no I/O or
// framework dependencies. The stub's branch logic (empty lists, random selection) is
// more clearly tested in isolation than through the full HTTP → controller → service
// chain, and the stub is @Primary when default profile is active, making it exercised
// by E2E but without deterministic coverage of its branching paths.
class ChaosEventGeneratorStubTest {

  private ChaosEventGeneratorStub stub;

  @BeforeEach
  void setUp() {
    stub = new ChaosEventGeneratorStub();
  }

  @Test
  void generateWithEmptyInputsUsesDefaults() {
    ChaosEvent event = stub.generate(emptyList(), emptyList(), null, null, null);

    assertThat(event).isNotNull();
    assertThat(event.headline()).isNotBlank();
    assertThat(event.symbol()).isEqualTo("GMEE");
    assertThat(event.sourceHeadline()).isEqualTo("Market Pulse");
    assertThat(event.impactPercent()).isNotNull();
    assertThat(event.explanation()).isNotBlank();
    assertThat(event.occurredAt()).isNotNull();
    assertThat(event.type()).isNotNull();
    assertThat(event.severity()).isNotNull();
  }

  @Test
  void generateWithHeadlinesUsesHeadlineSource() {
    ChaosEvent event =
        stub.generate(
            List.of(
                new NewsHeadline("Breaking News", "Source1", "tech", "https://example.com", now())),
            emptyList(),
            null,
            null,
            null);

    assertThat(event.sourceHeadline()).isEqualTo("Breaking News");
    assertThat(event.symbol()).isEqualTo("GMEE");
  }

  @Test
  void generateWithStocksPicksSymbolFromStocks() {
    ChaosEvent event =
        stub.generate(
            emptyList(),
            List.of(
                new StockPrice(
                    "TEND",
                    "Tendies",
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(99),
                    BigDecimal.ONE,
                    BigDecimal.valueOf(1.01),
                    now())),
            null,
            null,
            null);

    assertThat(event.symbol()).isEqualTo("TEND");
    assertThat(event.sourceHeadline()).isEqualTo("Market Pulse");
  }

  @Test
  void generateReturnsNonNullFields() {
    ChaosEvent event =
        stub.generate(
            List.of(new NewsHeadline("Headline1", "Src1", "biz", "https://a.com", now())),
            List.of(
                new StockPrice(
                    "DOGE",
                    "Dogecoin",
                    BigDecimal.valueOf(50),
                    BigDecimal.valueOf(48),
                    BigDecimal.valueOf(2),
                    BigDecimal.valueOf(4.0),
                    now())),
            null,
            null,
            null);

    assertThat(event.headline()).isEqualTo("Meme Stonks Go Brrr!");
    assertThat(event.affectedSymbols()).isNotEmpty();
    assertThat(event.impactPercent()).isBetween(BigDecimal.valueOf(15), BigDecimal.valueOf(35));
  }
}
