package dev.pollito.stonks_java.trade.adapter.in.rest;

import dev.pollito.stonks_java.portfolio.domain.GameResetEvent;
import dev.pollito.stonks_java.trade.application.port.out.TradeHistoryPortOut;
import dev.pollito.stonks_java.trade.application.port.out.TradePortfolioStatePortOut;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortfolioResetController {

  private static final long PORTFOLIO_ID = 1L;

  private final TradePortfolioStatePortOut tradePortfolioStatePortOut;
  private final TradeHistoryPortOut tradeHistoryPortOut;
  private final ApplicationEventPublisher events;

  @Value("${stonks.game.initial-cash:10000.00}")
  private BigDecimal initialCash;

  @PostMapping("/api/portfolio/reset")
  public ResponseEntity<Void> resetPortfolio() {
    tradePortfolioStatePortOut.resetPortfolio(PORTFOLIO_ID, initialCash);
    tradeHistoryPortOut.clearHistory();
    events.publishEvent(new GameResetEvent());
    return ResponseEntity.ok().build();
  }
}
