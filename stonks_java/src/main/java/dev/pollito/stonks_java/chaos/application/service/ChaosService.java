package dev.pollito.stonks_java.chaos.application.service;

import dev.pollito.stonks_java.chaos.application.port.in.ChaosPortIn;
import dev.pollito.stonks_java.chaos.application.port.out.ChaosEventGeneratorPortOut;
import dev.pollito.stonks_java.chaos.domain.ChaosEvent;
import dev.pollito.stonks_java.chaos.domain.ChaosEventTriggered;
import dev.pollito.stonks_java.chaos.domain.ChaosLevel;
import dev.pollito.stonks_java.news.application.port.in.NewsPortIn;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChaosService implements ChaosPortIn {

  private static final int MAX_HISTORY = 100;

  private final NewsPortIn newsPortIn;
  private final StockPortIn stockPortIn;
  private final ChaosEventGeneratorPortOut chaosEventGenerator;
  private final ApplicationEventPublisher eventPublisher;

  private final AtomicReference<ChaosLevel> currentLevel =
      new AtomicReference<>(ChaosLevel.PAPER_HANDS);
  private final ArrayDeque<ChaosEvent> history = new ArrayDeque<>(MAX_HISTORY);

  @Override
  public ChaosEvent triggerEvent() {
    ChaosEvent event =
        chaosEventGenerator.generate(newsPortIn.getHeadlines(), stockPortIn.getStocks()).get();

    stockPortIn.applyImpact(event.symbol(), event.impactPercent());
    eventPublisher.publishEvent(new ChaosEventTriggered(event));

    synchronized (history) {
      if (history.size() >= MAX_HISTORY) {
        history.removeFirst();
      }
      history.addLast(event);
    }

    return event;
  }

  @Override
  public List<ChaosEvent> getHistory() {
    synchronized (history) {
      return List.copyOf(history);
    }
  }

  @Override
  public ChaosLevel getCurrentLevel() {
    return currentLevel.get();
  }

  @Override
  public void setLevel(ChaosLevel level) {
    currentLevel.set(level);
  }
}
