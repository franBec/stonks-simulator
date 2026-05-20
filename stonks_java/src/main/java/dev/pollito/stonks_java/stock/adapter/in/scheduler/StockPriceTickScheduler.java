package dev.pollito.stonks_java.stock.adapter.in.scheduler;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceTickScheduler {
  private final StockPortIn stockPortIn;
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Scheduled(fixedRateString = "${stonks.market.simulation.interval-ms:5000}")
  void tick() {
    if (!running.compareAndSet(false, true)) {
      log.warn("Skipping price tick — previous tick still running");
      return;
    }
    try {
      log.debug("Running price tick");
      stockPortIn.simulate();
    } finally {
      running.set(false);
    }
  }
}
