package dev.pollito.stonks_java.chaosevent.adapter.out;

import dev.pollito.stonks_java.chaosevent.application.port.out.ChaoticEventGeneratorPortOut;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(prefix = "stonks.adapters", name = "ai", havingValue = "real")
@RequiredArgsConstructor
@Slf4j
public class ChaoticEventGeneratorCompositeAdapter implements ChaoticEventGeneratorPortOut {

  private final ChaoticEventGeneratorOpenRouterAdapter openRouter;
  private final ChaoticEventGeneratorFallbackAdapter fallback;

  @Override
  public ChaoticEvent generate(
      List<NewsHeadline> headlines,
      List<StockPrice> stocks,
      ChaoticEventType type,
      ChaoticEventSeverity severity,
      String targetSymbol) {
    try {
      return openRouter.generate(headlines, stocks, type, severity, targetSymbol);
    } catch (Exception e) {
      log.error("Primary chaos generator failed, using fallback", e);
      return fallback.generate(headlines, stocks, type, severity, targetSymbol);
    }
  }
}
