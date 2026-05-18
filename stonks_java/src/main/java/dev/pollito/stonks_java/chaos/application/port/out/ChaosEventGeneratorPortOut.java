package dev.pollito.stonks_java.chaos.application.port.out;

import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;

public interface ChaosEventGeneratorPortOut {
  ChaosEvent generate(List<NewsHeadline> headlines, List<StockPrice> stocks);
}
