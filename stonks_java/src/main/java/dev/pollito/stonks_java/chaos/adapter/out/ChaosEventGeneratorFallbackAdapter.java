package dev.pollito.stonks_java.chaos.adapter.out;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosEventSeverity;
import dev.pollito.stonks_java.chaos.domain.ChaosEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
@Slf4j
public class ChaosEventGeneratorFallbackAdapter implements ChaosEventGeneratorPortOut {

  @Override
  public ChaosEvent generate(
      List<NewsHeadline> headlines,
      List<StockPrice> stocks,
      ChaosEventType type,
      ChaosEventSeverity severity,
      String targetSymbol) {
    log.warn("Using fallback chaos event catalog");
    return ChaosEventFallbackGenerator.generate(headlines, stocks, type, severity, targetSymbol);
  }
}
