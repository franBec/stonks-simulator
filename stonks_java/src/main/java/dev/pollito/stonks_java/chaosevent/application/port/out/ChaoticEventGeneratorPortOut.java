package dev.pollito.stonks_java.chaosevent.application.port.out;

import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;

public interface ChaoticEventGeneratorPortOut {
  ChaoticEvent generate(
      List<NewsHeadline> headlines,
      List<StockPrice> stocks,
      ChaoticEventType type,
      ChaoticEventSeverity severity,
      String targetSymbol);
}
