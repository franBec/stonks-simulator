package dev.pollito.stonks_java.chaos.adapter.out;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile({"integrated", "production"})
@RequiredArgsConstructor
@Slf4j
public class ChaosEventGeneratorCompositeAdapter implements ChaosEventGeneratorPortOut {

  private final ChaosEventGeneratorOpenRouterAdapter openRouter;
  private final ChaosEventGeneratorFallbackAdapter fallback;

  @Override
  public ChaosEvent generate(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    try {
      return openRouter.generate(headlines, stocks);
    } catch (Exception e) {
      log.error("Primary chaos generator failed, using fallback", e);
      return fallback.generate(headlines, stocks);
    }
  }
}
