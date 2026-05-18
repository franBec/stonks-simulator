package dev.pollito.stonks_java.chaos.adapter.out;

import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.news.domain.NewsHeadline;
import dev.pollito.stonks_java.stock.domain.StockPrice;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class ChaosEventGeneratorCompositeAdapter implements ChaosEventGeneratorPortOut {

  private final ChaosEventGeneratorPortOut primary;
  private final ChaosEventGeneratorFallbackAdapter fallback;

  @Override
  public Optional<ChaosEvent> generate(List<NewsHeadline> headlines, List<StockPrice> stocks) {
    try {
      return primary
          .generate(headlines, stocks)
          .or(() -> Optional.of(fallback.generate(headlines, stocks)));
    } catch (Exception e) {
      log.error("Primary chaos generator failed, using fallback", e);
      return Optional.of(fallback.generate(headlines, stocks));
    }
  }
}
