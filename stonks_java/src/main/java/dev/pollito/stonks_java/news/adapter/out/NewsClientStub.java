package dev.pollito.stonks_java.news.adapter.out;

import static java.util.List.of;

import dev.pollito.stonks_java.news.application.port.out.NewsClientPortOut;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    prefix = "stonks.adapters",
    name = "news",
    havingValue = "stub",
    matchIfMissing = true)
@Slf4j
public class NewsClientStub implements NewsClientPortOut {

  private final AtomicInteger counter = new AtomicInteger(0);

  private static final List<List<NewsHeadline>> ROTATING_HEADLINES =
      of(
          of(
              new NewsHeadline(
                  "Fed Holds Interest Rates Steady",
                  "Bloomberg",
                  "economy",
                  "https://example.com/fed",
                  OffsetDateTime.now()),
              new NewsHeadline(
                  "Tech Stocks Rally on AI Optimism",
                  "Reuters",
                  "markets",
                  "https://example.com/tech-rally",
                  OffsetDateTime.now())),
          of(
              new NewsHeadline(
                  "Oil Prices Surge Amid Supply Concerns",
                  "CNBC",
                  "commodities",
                  "https://example.com/oil",
                  OffsetDateTime.now()),
              new NewsHeadline(
                  "Bitcoin Breaks $100K Milestone",
                  "CoinDesk",
                  "crypto",
                  "https://example.com/btc",
                  OffsetDateTime.now())),
          of(
              new NewsHeadline(
                  "Housing Market Shows Signs of Cooling",
                  "WSJ",
                  "economy",
                  "https://example.com/housing",
                  OffsetDateTime.now()),
              new NewsHeadline(
                  "Retail Sales Beat Expectations",
                  "Bloomberg",
                  "markets",
                  "https://example.com/retail",
                  OffsetDateTime.now())));

  @Override
  public List<NewsHeadline> fetchHeadlines() {
    log.warn("Using stub for NewsClientPortOut — returning canned headlines");
    return ROTATING_HEADLINES.get(counter.getAndIncrement() % ROTATING_HEADLINES.size());
  }
}
