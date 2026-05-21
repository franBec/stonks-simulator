package dev.pollito.stonks_java.chaos.adapter.out;

import static dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity.MEDIUM;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventType.HYPE_WAVE;
import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;
import static java.util.List.of;
import static java.util.concurrent.ThreadLocalRandom.current;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity;
import dev.pollito.stonks_java.chaos.domain.ChaosEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(
    prefix = "stonks.adapters",
    name = "ai",
    havingValue = "stub",
    matchIfMissing = true)
@Slf4j
public class ChaosEventGeneratorStub implements ChaosEventGeneratorPortOut {

  @Override
  public ChaosEvent generate(
      List<NewsHeadline> headlines,
      List<StockPrice> stocks,
      ChaosEventType type,
      ChaosEventSeverity severity,
      String targetSymbol) {
    log.warn("Using stub for ChaosEventGeneratorPortOut — returning canned chaos event");
    String source =
        headlines.isEmpty()
            ? "Market Pulse"
            : headlines.get(current().nextInt(headlines.size())).title();
    String symbol =
        targetSymbol != null
            ? targetSymbol
            : stocks.isEmpty() ? "GMEE" : stocks.get(current().nextInt(stocks.size())).symbol();
    return new ChaosEvent(
        "Meme Stonks Go Brrr!",
        symbol,
        valueOf(15.0 + current().nextDouble(20.0)),
        "The algo detected extreme meme energy in the market. To the moon!",
        of(symbol),
        source,
        now(),
        type != null ? type : HYPE_WAVE,
        severity != null ? severity : MEDIUM);
  }
}
