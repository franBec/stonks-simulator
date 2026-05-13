package dev.pollito.stonks_java.stock.adapter.in.scheduler;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceTickScheduler {
  private final StockPortIn stockPortIn;

  @Scheduled(fixedRateString = "${stonks.market.simulation.interval-ms:2000}")
  void tick() {
    log.debug("Running price tick");
    stockPortIn.simulate();
  }
}
