package dev.pollito.stonks_java.stocks.adapter.in.scheduler;

import dev.pollito.stonks_java.stocks.application.service.StockPriceTickService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceTickScheduler {
  private final StockPriceTickService stockPriceTickService;

  @Scheduled(fixedRateString = "${stonks.market.simulation.interval-ms:2000}")
  void runPriceSimulation() {
    log.debug("Running price simulation tick");
    stockPriceTickService.simulate();
  }
}
