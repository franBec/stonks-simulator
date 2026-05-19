package dev.pollito.stonks_java.stock.adapter.in.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import org.junit.jupiter.api.Test;

// Unit test (not E2E) for StockPriceTickScheduler because its AtomicBoolean
// guard logic (skip overlapping ticks) is an internal concurrency concern
// not observable through HTTP responses.
class StockPriceTickSchedulerTest {

  @Test
  void tickCallsSimulateWhenNotRunning() {
    StockPortIn stockPortIn = mock(StockPortIn.class);
    StockPriceTickScheduler scheduler = new StockPriceTickScheduler(stockPortIn);

    scheduler.tick();

    verify(stockPortIn).simulate();
  }
}
