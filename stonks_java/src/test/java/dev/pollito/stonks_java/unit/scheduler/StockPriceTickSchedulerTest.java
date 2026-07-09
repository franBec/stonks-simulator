package dev.pollito.stonks_java.unit.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.pollito.stonks_java.config.GameStateService;
import dev.pollito.stonks_java.stock.adapter.in.scheduler.StockPriceTickScheduler;
import dev.pollito.stonks_java.stock.application.port.in.StockPortIn;
import org.junit.jupiter.api.Test;

class StockPriceTickSchedulerTest {

  @Test
  void tickCallsSimulateWhenNotRunningAndPlaying() {
    StockPortIn stockPortIn = mock(StockPortIn.class);
    GameStateService gameStateService = mock(GameStateService.class);
    when(gameStateService.isPlaying()).thenReturn(true);
    StockPriceTickScheduler scheduler = new StockPriceTickScheduler(stockPortIn, gameStateService);

    scheduler.tick();

    verify(gameStateService).isPlaying();
    verify(stockPortIn).simulate();
  }

  @Test
  void tickSkipsSimulateWhenGameIsOver() {
    StockPortIn stockPortIn = mock(StockPortIn.class);
    GameStateService gameStateService = mock(GameStateService.class);
    when(gameStateService.isPlaying()).thenReturn(false);
    StockPriceTickScheduler scheduler = new StockPriceTickScheduler(stockPortIn, gameStateService);

    scheduler.tick();
  }
}
