package dev.pollito.stonks_java.chaosevent.application.service;

import static java.time.OffsetDateTime.now;

import dev.pollito.stonks_java.chaosevent.application.port.in.ChaoseventPortIn;
import dev.pollito.stonks_java.chaosevent.application.port.out.ChaoticEventGeneratorPortOut;
import dev.pollito.stonks_java.chaosevent.application.port.out.ChaoticIncidentPortOut;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEvent;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventHistory;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventSeverity;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventTriggered;
import dev.pollito.stonks_java.chaosevent.domain.ChaoticEventType;
import dev.pollito.stonks_java.news.application.port.in.NewsPortIn;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import dev.pollito.stonks_java.stock.domain.ApplyStockImpact;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChaoseventService implements ChaoseventPortIn {

  private final NewsPortIn newsPortIn;
  private final StockPortIn stockPortIn;
  private final ChaoticEventGeneratorPortOut chaoticEventGenerator;
  private final ChaoticIncidentPortOut chaoticIncidentPortOut;
  private final ApplicationEventPublisher eventPublisher;

  private final ChaoticEventHistory history = new ChaoticEventHistory();

  @PostConstruct
  public void initialize() {
    history.loadFrom(chaoticIncidentPortOut.loadHistory());
  }

  @Override
  public ChaoticEvent triggerEvent() {
    return triggerEvent(null, null, null);
  }

  @Override
  public ChaoticEvent triggerEvent(
      ChaoticEventType type, ChaoticEventSeverity severity, String targetSymbol) {
    ChaoticEvent generated =
        chaoticEventGenerator.generate(
            newsPortIn.getHeadlines(), stockPortIn.getStocks(), type, severity, targetSymbol);
    ChaoticEvent event =
        new ChaoticEvent(
            generated.headline(),
            generated.symbol(),
            generated.impactPercent(),
            generated.explanation(),
            generated.affectedSymbols(),
            generated.sourceHeadline(),
            now(),
            generated.type() != null ? generated.type() : type,
            generated.severity() != null ? generated.severity() : severity);

    eventPublisher.publishEvent(new ApplyStockImpact(event.symbol(), event.impactPercent()));
    eventPublisher.publishEvent(new ChaoticEventTriggered(event));

    history.add(event);
    chaoticIncidentPortOut.recordEvent(event);

    return event;
  }

  @Override
  public List<ChaoticEvent> getHistory() {
    return history.getAll();
  }
}
