package dev.pollito.stonks_java.chaosevent.adapter.out;

import dev.pollito.stonks_java.chaosevent.application.port.out.ChaoticEventGeneratorPortOut;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
@Slf4j
public class ChaoticEventGeneratorFallbackAdapter implements ChaoticEventGeneratorPortOut {

  @Override
  public ChaoticEvent generate(
      List<NewsHeadline> headlines,
      List<StockPrice> stocks,
      ChaoticEventType type,
      ChaoticEventSeverity severity,
      String targetSymbol) {
    log.warn("Using fallback chaos event catalog");
    return ChaoticEventFallbackGenerator.generate(headlines, stocks, type, severity, targetSymbol);
  }
}
