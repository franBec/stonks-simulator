package dev.pollito.stonks_java.chaos.adapter.out;

import static java.util.concurrent.ThreadLocalRandom.current;

import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChaosEventGeneratorFallbackAdapter {

  private static final List<FallbackEvent> CATALOG =
      List.of(
          new FallbackEvent(
              "COBOL Programmer Retired",
              "A legacy COBOL programmer retired, taking critical knowledge. COBL holders panic-buy.",
              "COBL",
              50.0,
              List.of("COBL")),
          new FallbackEvent(
              "Bug Found in Production",
              "A critical production bug was discovered. BUGS investors panic-sell.",
              "BUGS",
              -40.0,
              List.of("BUGS")),
          new FallbackEvent(
              "WSB Discovers Hidden Gem",
              "WallStreetBets discovered a hidden gem. Diamond hands activated!",
              null,
              30.0,
              null),
          new FallbackEvent(
              "Market Crash",
              "Panic selling across all markets as fear takes over.",
              null,
              -20.0,
              null),
          new FallbackEvent(
              "Elon Tweets Doge Again",
              "Elon Musk tweeted about memecoins, causing a buying frenzy.",
              "DOGE",
              25.0,
              List.of("DOGE")),
          new FallbackEvent(
              "Fed Signals Rate Cut",
              "The Federal Reserve signals a dovish pivot, boosting risk assets.",
              null,
              8.0,
              null),
          new FallbackEvent(
              "Short Squeeze Incoming",
              "Short interest hits critical levels. Squeeze imminent!",
              null,
              35.0,
              null),
          new FallbackEvent(
              "CEO Suddenly Steps Down",
              "The CEO resigned unexpectedly, sending shockwaves through the market.",
              null,
              -25.0,
              null),
          new FallbackEvent(
              "AI Model Goes Rogue",
              "An AI trading model started buying its own ticker. Singularity priced in.",
              "AI",
              40.0,
              List.of("AI")),
          new FallbackEvent(
              "SEC Launches Investigation",
              "Regulators are investigating suspicious trading activity.",
              null,
              -30.0,
              null),
          new FallbackEvent(
              "Hedge Fund Liquidates Position",
              "A major hedge fund is forced to liquidate. Blood in the streets.",
              null,
              -15.0,
              null),
          new FallbackEvent(
              "Earnings Blow Past Estimates",
              "Quarterly earnings crushed expectations. Moon landing confirmed.",
              null,
              22.0,
              null),
          new FallbackEvent(
              "Insider Buying Spree Detected",
              "C-suite executives are loading up on shares. They know something.",
              null,
              18.0,
              null),
          new FallbackEvent(
              "Geopolitical Tensions Spike",
              "Rising geopolitical tensions rattle global markets.",
              null,
              -10.0,
              null),
          new FallbackEvent(
              "Meme Stock Split Announced",
              "A 10-for-1 stock split makes shares more accessible to retail investors.",
              null,
              12.0,
              null));

  public ChaosEvent generate(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    log.warn("Using fallback chaos event catalog");
    FallbackEvent fb = CATALOG.get(current().nextInt(CATALOG.size()));
    String source =
        headlines.isEmpty()
            ? "Market Pulse"
            : headlines.get(current().nextInt(headlines.size())).title();
    String symbol =
        fb.symbol != null
            ? fb.symbol
            : stocks.isEmpty() ? "GMEE" : stocks.get(current().nextInt(stocks.size())).symbol();
    List<String> affected =
        fb.affectedSymbols != null
            ? fb.affectedSymbols
            : stocks.isEmpty()
                ? List.of("GMEE")
                : List.of(stocks.get(current().nextInt(stocks.size())).symbol());
    return new ChaosEvent(
        fb.headline,
        symbol,
        BigDecimal.valueOf(fb.impactPercent),
        fb.explanation,
        affected,
        source,
        OffsetDateTime.now());
  }

  private record FallbackEvent(
      String headline,
      String explanation,
      String symbol,
      double impactPercent,
      List<String> affectedSymbols) {}
}
