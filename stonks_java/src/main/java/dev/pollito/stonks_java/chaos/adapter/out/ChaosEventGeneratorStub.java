package dev.pollito.stonks_java.chaos.adapter.out;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
  public ChaosEvent generate(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    log.warn("Using stub for ChaosEventGeneratorPortOut — returning canned chaos event");
    String source =
        headlines.isEmpty()
            ? "Market Pulse"
            : headlines.get(ThreadLocalRandom.current().nextInt(headlines.size())).title();
    String symbol =
        stocks.isEmpty()
            ? "GMEE"
            : stocks.get(ThreadLocalRandom.current().nextInt(stocks.size())).symbol();
    return new ChaosEvent(
        "Meme Stonks Go Brrr!",
        symbol,
        BigDecimal.valueOf(15.0 + ThreadLocalRandom.current().nextDouble(20.0)),
        "The algo detected extreme meme energy in the market. To the moon!",
        List.of(symbol),
        source,
        OffsetDateTime.now());
  }
}
