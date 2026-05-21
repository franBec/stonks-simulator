package dev.pollito.stonks_java.chaos.application.service;

import static java.math.BigDecimal.valueOf;
import static java.time.OffsetDateTime.now;

import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosEventHistory;
import dev.pollito.stonks_java.chaos.domain.ChaosEventTriggered;
import dev.pollito.stonks_java.chaos.domain.ChaosLevel;
import dev.pollito.stonks_java.news.application.port.in.NewsPortIn;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChaosService implements ChaosPortIn {

  private final NewsPortIn newsPortIn;
  private final StockPortIn stockPortIn;
  private final ChaosEventGeneratorPortOut chaosEventGenerator;
  private final ApplicationEventPublisher eventPublisher;

  private final AtomicReference<ChaosLevel> currentLevel =
      new AtomicReference<>(ChaosLevel.PAPER_HANDS);
  private final ChaosEventHistory history = new ChaosEventHistory();

  @Override
  public ChaosEvent triggerEvent() {
    ChaosEvent generated =
        chaosEventGenerator.generate(newsPortIn.getHeadlines(), stockPortIn.getStocks());
    ChaosEvent event =
        new ChaosEvent(
            generated.headline(),
            generated.symbol(),
            generated.impactPercent(),
            generated.explanation(),
            generated.affectedSymbols(),
            generated.sourceHeadline(),
            now());

    stockPortIn.applyImpact(event.symbol(), event.impactPercent());
    eventPublisher.publishEvent(new ChaosEventTriggered(event));

    history.add(event);

    return event;
  }

  @Override
  public List<ChaosEvent> getHistory() {
    return history.getAll();
  }

  @Override
  public ChaosLevel getCurrentLevel() {
    return currentLevel.get();
  }

  @Override
  public void setLevel(ChaosLevel level) {
    currentLevel.set(level);
    stockPortIn.setVolatilityMultiplier(valueOf(level.getVolatilityMultiplier()));
  }
}
