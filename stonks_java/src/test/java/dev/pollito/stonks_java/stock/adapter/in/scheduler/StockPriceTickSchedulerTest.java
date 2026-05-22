package dev.pollito.stonks_java.stock.adapter.in.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import org.junit.jupiter.api.Test;

// Low coverage on StockPriceTickScheduler.java is acceptable. The remaining
// branch (guard against overlapping ticks) depends on private mutable state
// (AtomicBoolean running) that can only be set via reflection, which we
// prefer to avoid.
class StockPriceTickSchedulerTest {

  @Test
  void tickCallsSimulateWhenNotRunning() {
    StockPortIn stockPortIn = mock(StockPortIn.class);
    StockPriceTickScheduler scheduler = new StockPriceTickScheduler(stockPortIn);

    scheduler.tick();

    verify(stockPortIn).simulate();
  }
}
