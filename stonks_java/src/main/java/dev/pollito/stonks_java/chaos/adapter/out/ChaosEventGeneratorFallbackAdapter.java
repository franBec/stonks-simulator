package dev.pollito.stonks_java.chaos.adapter.out;

import static dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity.CRITICAL;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity.HIGH;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity.MEDIUM;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventType.DUMP;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventType.HYPE_WAVE;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventType.NEWS_FLASH;
import static dev.pollito.stonks_java.chaos.domain.ChaosEventType.WHALE_ALERT;
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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
@Slf4j
public class ChaosEventGeneratorFallbackAdapter implements ChaosEventGeneratorPortOut {

  private static final List<FallbackEvent> CATALOG =
      of(
          new FallbackEvent(
              "COBOL Programmer Retired",
              "A legacy COBOL programmer retired, taking critical knowledge. COBL holders panic-buy.",
              "COBL",
              50.0,
              of("COBL"),
              HYPE_WAVE,
              CRITICAL),
          new FallbackEvent(
              "Bug Found in Production",
              "A critical production bug was discovered. BUGS investors panic-sell.",
              "BUGS",
              -40.0,
              of("BUGS"),
              DUMP,
              CRITICAL),
          new FallbackEvent(
              "WSB Discovers Hidden Gem",
              "WallStreetBets discovered a hidden gem. Diamond hands activated!",
              null,
              30.0,
              null,
              HYPE_WAVE,
              HIGH),
          new FallbackEvent(
              "Market Crash",
              "Panic selling across all markets as fear takes over.",
              null,
              -20.0,
              null,
              DUMP,
              CRITICAL),
          new FallbackEvent(
              "Elon Tweets Doge Again",
              "Elon Musk tweeted about memecoins, causing a buying frenzy.",
              "DOGE",
              25.0,
              of("DOGE"),
              HYPE_WAVE,
              HIGH),
          new FallbackEvent(
              "Fed Signals Rate Cut",
              "The Federal Reserve signals a dovish pivot, boosting risk assets.",
              null,
              8.0,
              null,
              NEWS_FLASH,
              MEDIUM),
          new FallbackEvent(
              "Short Squeeze Incoming",
              "Short interest hits critical levels. Squeeze imminent!",
              null,
              35.0,
              null,
              HYPE_WAVE,
              CRITICAL),
          new FallbackEvent(
              "CEO Suddenly Steps Down",
              "The CEO resigned unexpectedly, sending shockwaves through the market.",
              null,
              -25.0,
              null,
              NEWS_FLASH,
              HIGH),
          new FallbackEvent(
              "AI Model Goes Rogue",
              "An AI trading model started buying its own ticker. Singularity priced in.",
              "AI",
              40.0,
              of("AI"),
              WHALE_ALERT,
              CRITICAL),
          new FallbackEvent(
              "SEC Launches Investigation",
              "Regulators are investigating suspicious trading activity.",
              null,
              -30.0,
              null,
              NEWS_FLASH,
              CRITICAL),
          new FallbackEvent(
              "Hedge Fund Liquidates Position",
              "A major hedge fund is forced to liquidate. Blood in the streets.",
              null,
              -15.0,
              null,
              WHALE_ALERT,
              HIGH),
          new FallbackEvent(
              "Earnings Blow Past Estimates",
              "Quarterly earnings crushed expectations. Moon landing confirmed.",
              null,
              22.0,
              null,
              NEWS_FLASH,
              HIGH),
          new FallbackEvent(
              "Insider Buying Spree Detected",
              "C-suite executives are loading up on shares. They know something.",
              null,
              18.0,
              null,
              WHALE_ALERT,
              MEDIUM),
          new FallbackEvent(
              "Geopolitical Tensions Spike",
              "Rising geopolitical tensions rattle global markets.",
              null,
              -10.0,
              null,
              DUMP,
              MEDIUM),
          new FallbackEvent(
              "Meme Stock Split Announced",
              "A 10-for-1 stock split makes shares more accessible to retail investors.",
              null,
              12.0,
              null,
              HYPE_WAVE,
              MEDIUM));

  @Override
  public ChaosEvent generate(
      List<NewsHeadline> headlines,
      List<StockPrice> stocks,
      ChaosEventType type,
      ChaosEventSeverity severity,
      String targetSymbol) {
    log.warn("Using fallback chaos event catalog");
    List<FallbackEvent> candidates = CATALOG;
    if (type != null) {
      candidates = candidates.stream().filter(e -> e.type == type).collect(Collectors.toList());
    }
    if (severity != null) {
      candidates =
          candidates.stream().filter(e -> e.severity == severity).collect(Collectors.toList());
    }
    if (candidates.isEmpty()) {
      candidates = CATALOG;
    }
    FallbackEvent fb = candidates.get(current().nextInt(candidates.size()));
    String source =
        headlines.isEmpty()
            ? "Market Pulse"
            : headlines.get(current().nextInt(headlines.size())).title();
    String symbol =
        targetSymbol != null
            ? targetSymbol
            : fb.symbol != null
                ? fb.symbol
                : stocks.isEmpty() ? "GMEE" : stocks.get(current().nextInt(stocks.size())).symbol();
    List<String> affected =
        fb.affectedSymbols != null
            ? fb.affectedSymbols
            : stocks.isEmpty()
                ? of("GMEE")
                : of(stocks.get(current().nextInt(stocks.size())).symbol());
    return new ChaosEvent(
        fb.headline,
        symbol,
        valueOf(fb.impactPercent),
        fb.explanation,
        affected,
        source,
        now(),
        fb.type,
        fb.severity);
  }

  private record FallbackEvent(
      String headline,
      String explanation,
      String symbol,
      double impactPercent,
      List<String> affectedSymbols,
      ChaosEventType type,
      ChaosEventSeverity severity) {}
}
